package edu.ucsf.ctsi.r2r.jena;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.google.inject.Inject;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.ucsf.ctsi.r2r.jena.ResourceService;
import edu.ucsf.ctsi.r2r.jena.RDFXMLService;

// put this in r2r jar to be shared with Crosslinks, add something about storing it or not
public class FusekiCache implements ModelService, RDFXMLService, ResourceService {

	private static final String TIMESTAMP = "http://ucsf.edu/ontology/R2R#addedToCacheOn";
	private static final String TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
	private static final Logger LOG = Logger.getLogger(FusekiCache.class.getName());
	
	private static final String EXPIRED_TEMPLATE = "SELECT ?ac ?ts WHERE { <%s> <" + TIMESTAMP + "> ?ac. <%s> <" + TYPE + "> ?ts.}";
	
	private FusekiService fusekiService;
	private RDFXMLService rdfxmlService;
	private Map<String, Integer> expirationHours = new HashMap<String, Integer>();
		
	@Inject
	public FusekiCache(FusekiService fusekiService, RDFXMLService rdfxmlService) throws IOException {		
		this.fusekiService = fusekiService;
		this.rdfxmlService = rdfxmlService;
		
		Properties prop = new Properties();
		prop.load(FusekiCache.class.getResourceAsStream("/fusekiCache.properties"));		
		for (Object key : prop.keySet())  {
			expirationHours.put(key.toString(), Integer.parseInt(prop.getProperty(key.toString())));
		}
	}

	private Property createTimestamp(Model model) {
		return  model.createProperty(TIMESTAMP);
	}
	
	private Property createType(Model model) {
		return  model.createProperty(TYPE);
	}

	public boolean contains(String uri) {
		Model model = fusekiService.get(uri);
		return model.contains(ResourceFactory.createResource(uri), null);		
	}
	
	private Integer getCacheExpireHours(Resource resource) {
		Property typeProperty = createType(resource.getModel());
		StmtIterator types = resource.listProperties(typeProperty);
		while (types.hasNext()) {
			String type = types.next().getObject().toString();
			if (expirationHours.containsKey(type)) {
				return expirationHours.get(type);
			}
		}
		return null;
	}
	
	private Integer getCacheExpireHours(String type) {
		if (expirationHours.containsKey(type)) {
			return expirationHours.get(type);
		}
		return null;
	}
	
	private boolean hasExpired(Resource resource) {
		Integer expireHours = getCacheExpireHours(resource);
		// if it does not have an expiration date, grab a fresh one
        boolean resourceExpired = true;
		if (expireHours != null && resource.hasProperty(createTimestamp(resource.getModel())) ) {
			Literal writeTime = resource.getProperty(createTimestamp(resource.getModel())).getLiteral();
			DateTime expiresOn = new DateTime(writeTime.getLong()).plus(expireHours);
			resourceExpired = expiresOn.isBeforeNow();
		}
		return resourceExpired;
	}
	
	private boolean hasExpired(Literal writeTime, String type) {
		Integer expireHours = getCacheExpireHours(type);
		// if it does not have an expiration date, grab a fresh one
        boolean resourceExpired = true;
		if (expireHours != null && writeTime != null ) {
			DateTime expiresOn = new DateTime(writeTime.getLong()).plusHours(expireHours);
			resourceExpired = expiresOn.isBeforeNow();
		}
		return resourceExpired;
	}

	private boolean hasExpired(String uri) {
		ExpiredResultSetConsumer consumer = new ExpiredResultSetConsumer();
		fusekiService.select(String.format(EXPIRED_TEMPLATE, uri, uri), consumer);
		return consumer.getHasExpired();
	}

	// we return either a Model or a byte[] because its more efficient to let clients transform if and when necessary
	private Object getFreshItem(String rdfUrl, String uri) throws Exception  {
		// see if we have a recent copy of this
		byte[] body = ensureFreshItem(rdfUrl, uri);
		if (body != null) {
			return body;
		}
		else {
			return fusekiService.get(uri);
		}
	}

	// will make sure we have a fresh copy, and return the fresh copy if we grab it just now
	private byte[] ensureFreshItem(String rdfUrl, String uri) throws Exception {
		if (hasExpired(uri)) {
			// we might be asking fuseki to remove something it doesn't have, but this is fast so that is OK 
			fusekiService.delete(uri);
        	LOG.info("Expired " + uri);
    		try {
    			// if we get here, we need to add to cache
    			return addToCache(rdfUrl, uri);
    		}
    		catch (Exception e) {
    			LOG.log(Level.WARNING, "Could not access RDF from " + uri, e);
    		}
		}		
		return null;
	}
	
	private byte[] addToCache(String rdfUrl, String uri) throws Exception {
		byte[] body = rdfxmlService.getRDFXML(rdfUrl);
		if (body != null) {
			fusekiService.add(body);
			// now add the timestamp
			fusekiService.update("INSERT DATA { <" + uri + "> <" + TIMESTAMP + "> " + new DateTime().getMillis() + ".}");
		}
		return body;		
	}
	
	public byte[] getRDFXML(String uri) throws Exception {
		return getRDFXML(uri, uri);
	}
	
	public byte[] getRDFXML(String rdfUrl, String uri) throws Exception {
		Object obj = getFreshItem(rdfUrl, uri);
		if (obj instanceof Model) {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			((Model)obj).write(stream);
			stream.flush();
			stream.close();
			return stream.toByteArray();
		}
		else if (obj instanceof byte[]) {
			return (byte[])obj;
		}
		return null;
	}

	public Model getModel(String uri) throws Exception {
		return getModel(uri, uri);
	}
	
	public Model getModel(String rdfUrl, String uri) throws Exception {
		Object obj = getFreshItem(rdfUrl, uri);
		if (obj instanceof Model) {
			return (Model)obj;
		}
		else if (obj instanceof byte[]) {
			return ModelFactory.createDefaultModel().read(new ByteArrayInputStream((byte[])obj), null);
		}
		return null;
	}

	public Resource getResource(String uri) throws Exception {
		return getResource(uri, uri);
	}

	public Resource getResource(String rdfUrl, String uri) throws Exception {
		Model model = getModel(rdfUrl, uri);
		return model != null ? model.createResource(uri) : null;
	}	
	
	public Map<String, String> getFields(String uri, Set<String> fields) throws Exception {
		return getFields(uri, uri, fields);
	}
	
	public Map<String, String> getFields(String rdfUrl, String uri, Set<String> fields) throws Exception {
		if (fields.size() == 0) {
			return null;
		}
		ensureFreshItem(rdfUrl, uri);
		String select = "SELECT";
		String where = " WHERE {";
		int cnt = 0;
		Map<String, String> varNameMap = new HashMap<String, String>();
		for (String field : fields) {
			String var = "o" + cnt++;
			select += " ?" + var;
			where += "OPTIONAL {<" + uri + "> <" + field + "> ?" + var + "} "; 			
			varNameMap.put(var, field);
		}
		
		SelectResultSetConsumer consumer = new SelectResultSetConsumer(varNameMap);
		fusekiService.select(select + where + "}", consumer);
		return consumer.getData();
	}
	
	public static void main(String[] args) {
		try  {								
			// get these first
			FusekiService fs = new HttpClientFusekiService("http://localhost:3030/profiles");
			FusekiCache fc = new FusekiCache(fs, null);
			fc.hasExpired("http://stage-profiles.ucsf.edu/profiles200/profile/366860");
			fc.hasExpired("http://stage-profiles.ucsf.edu/profiles200/profile/123");
			
			Set<String> fields = new HashSet<String>();
			fields.add("http://ucsf.edu/ontology/R2R#addedToCacheOn");
			fields.add("http://www.w3.org/2000/01/rdf-schema#label");
			fields.add("http://www.w3.org/2000/01/rdf-schema#foo");
			Map<String, String> values =fc.getFields("http://stage-profiles.ucsf.edu/profiles200/profile/365069", fields);
			for (String n : values.keySet()) {
				System.out.println(n + ":" + values.get(n));
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}		
	}	
	
	private class ExpiredResultSetConsumer implements ResultSetConsumer {
		private boolean hasExpired = true;
		
		public void useResultSet(ResultSet rs) {
			while (rs.hasNext()) {
				QuerySolution qs = rs.next();
				RDFNode n = qs.get("?ts");
				Integer cacheExpire = getCacheExpireHours(n.toString());
				if (cacheExpire != null) {
					Literal writeTime = qs.contains("?ac") ? qs.getLiteral("?ac") : null;
					hasExpired = hasExpired(writeTime, n.toString());
					break;
				}
			}				
		}	
		
		public boolean getHasExpired() {
			return hasExpired;
		}		
	}

	private class SelectResultSetConsumer implements ResultSetConsumer {
		private Map<String, String> data = new HashMap<String, String>();
		private Map<String, String> varNameMap;
		
		public SelectResultSetConsumer(Map<String, String> varNameMap) {
			this.varNameMap = varNameMap;
		}
		
		public void useResultSet(ResultSet rs) {
			if (rs.hasNext()) {
				QuerySolution qs = rs.next();
				Iterator<String> varNames = qs.varNames();
				while (varNames.hasNext()) {
					String var = varNames.next();
					RDFNode n = qs.get(var);
					data.put(varNameMap.get(var), n.toString());
				}
			}
		}	
		
		public Map<String, String> getData() {
			return data;
		}		
	}
}
