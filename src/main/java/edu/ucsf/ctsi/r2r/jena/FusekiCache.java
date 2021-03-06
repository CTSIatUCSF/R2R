package edu.ucsf.ctsi.r2r.jena;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.datatypes.xsd.XSDDateTime;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import com.google.inject.Inject;

import edu.ucsf.ctsi.r2r.R2RConstants;
import edu.ucsf.ctsi.r2r.R2ROntology;
import edu.ucsf.ctsi.r2r.jena.ResourceService;
import edu.ucsf.ctsi.r2r.jena.RDFXMLService;

// put this in r2r jar to be shared with Crosslinks, add something about storing it or not
public class FusekiCache implements ModelService, RDFXMLService, ResourceService, R2RConstants {

	private static final String DEFAULT = "DEFAULT";
	private static final Logger LOG = Logger.getLogger(FusekiCache.class.getName());
	
	private static final String EXPIRED_TEMPLATE = "SELECT ?ac ?ts WHERE { <%s> <" + R2R_ADDED_TO_CACHE + "> ?ac. <%s> <" + RDF_TYPE + "> ?ts}";
	private static final String EMAIL_ENCRYPTED_TEMPLATE = "SELECT ?ee WHERE { <%s> <" + PRNS_EMAIL_ENCRYPTED + "> ?ee}";
	private static final String EXPIRED_ASK_TEMPLATE = "ASK { <%1$s> <" + R2R_ADDED_TO_CACHE + "> ?ac. FILTER(?ac > \"%2$s\") }";
	
	private SparqlQueryClient sparqlQueryClient;
	private SparqlPostClient fusekiClient;
	private RDFXMLService rdfxmlService;
	private DecryptionService decryptionService;
	private Map<String, Integer> expirationHours = new HashMap<String, Integer>();
	private final Integer defaultExpirationHours;
		
	@Inject
	public FusekiCache(SparqlQueryClient sparqlQueryClient, SparqlPostClient fusekiClient, RDFXMLService rdfxmlService, DecryptionService decryptionService) throws Exception {
		this.sparqlQueryClient = sparqlQueryClient;
		this.fusekiClient = fusekiClient;
		this.rdfxmlService = rdfxmlService;
		this.decryptionService = decryptionService;
		
		// add the model to the cache
		fusekiClient.add(R2ROntology.createR2ROntModel());
		
		Properties prop = new Properties();
		prop.load(FusekiCache.class.getResourceAsStream("/fusekiCache.properties"));		
		for (Object key : prop.keySet())  {
			expirationHours.put(key.toString(), Integer.parseInt(prop.getProperty(key.toString())));
		}
		defaultExpirationHours = prop.containsKey(DEFAULT) ? Integer.parseInt(prop.getProperty(DEFAULT)) : null;
	}

	public boolean contains(String uri) {
		String query = "ASK {<" + uri + "> <" + R2R_ADDED_TO_CACHE + "> ?o}";
		return sparqlQueryClient.ask(query);
	}
	
	private Integer getCacheExpireHours(String type) {
		if (expirationHours.containsKey(type)) {
			return expirationHours.get(type);
		}
		return defaultExpirationHours;
	}
	
	private boolean hasExpired(Literal writeTime, String type) {
		Integer expireHours = getCacheExpireHours(type);
		// if it does not have an expiration date, grab a fresh one
		DateTime expiresOn = null;
		if (expireHours != null && writeTime != null ) {
			try {
				expiresOn = new DateTime(((XSDDateTime)writeTime.getValue()).asCalendar()).plusHours(expireHours);			
			}
			catch (Exception e) {
				LOG.log(Level.WARNING, "writeTime exception", e);				
				LOG.log(Level.WARNING, "writeTime class:" + writeTime.getValue().getClass());
				LOG.log(Level.WARNING, "writeTime datatype:" + writeTime.getDatatypeURI());
				LOG.log(Level.WARNING, "writeTime value :" + writeTime.getValue());
			}
		}
		return expiresOn != null ? expiresOn.isBeforeNow() : true;
	}

	// see if sparql ASK will work better
	private boolean hasExpired(String uri) throws Exception {
		//return sparqlQueryClient.ask(String.format(EXPIRED_ASK_TEMPLATE, uri, uri));
		ExpiredResultSetConsumer consumer = new ExpiredResultSetConsumer();
		sparqlQueryClient.select(String.format(EXPIRED_TEMPLATE, uri, uri), consumer);
		return consumer.getHasExpired();
	}
	
	private String getEncyptedEmail(String uri) throws Exception {
		//return sparqlQueryClient.ask(String.format(EXPIRED_ASK_TEMPLATE, uri, uri));
		EmailEncryptedResultSetConsumer consumer = new EmailEncryptedResultSetConsumer();
		sparqlQueryClient.select(String.format(EMAIL_ENCRYPTED_TEMPLATE, uri), consumer);
		return consumer.getEmailEncrypted();
	}

	// we return either a Model or a byte[] because its more efficient to let clients transform if and when necessary
	private Object getFreshItem(String rdfUrl, String uri) throws Exception  {
		// see if we have a recent copy of this
		byte[] body = ensureFreshItem(rdfUrl, uri);
		if (body != null) {
			return body;
		}
		else {
			return sparqlQueryClient.describe(uri);
		}
	}

	// will make sure we have a fresh copy, and return the fresh copy if we grab it just now
	private byte[] ensureFreshItem(String rdfUrl, String uri) throws Exception {
		if (hasExpired(uri)) {
			// we might be asking fuseki to remove something it doesn't have, but this is fast so that is OK 
			fusekiClient.deleteSubject(uri);
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
			fusekiClient.add(body);
			
			// see if we need to decrypt email
			String emailEncrypted = getEncyptedEmail(uri);
			if (emailEncrypted != null) {
				fusekiClient.update("INSERT { <" + uri + "> <" + VIVO_EMAIL + "> \"" + decryptionService.decrypt(emailEncrypted) + "\"} WHERE {}");
			}
						
			// now add the timestamp
			fusekiClient.update("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" + 
					"INSERT { <" + uri + "> <" + R2R_ADDED_TO_CACHE + "> ?typedDate} WHERE {BIND (xsd:dateTime(\"" + 
					ISODateTimeFormat.dateTime().print(new DateTime()) + "\") as ?typedDate)}");			
//			fusekiClient.update("INSERT DATA { <" + uri + "> <" + R2R_ADDED_TO_CACHE + "> \"" + 
//					R2ROntology.createDefaultModel().createTypedLiteral(Calendar.getInstance()) + "\"}");
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
			// this should already have all the namespaces in it
			return R2ROntology.createDefaultModel().read(new ByteArrayInputStream((byte[])obj), null);
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
	
	public Model getModel(String uri, Set<String> fields) throws Exception {
		return getModel(uri, uri, fields);
	}
	
	public Model getModel(String rdfUrl, String uri, Set<String> fields) throws Exception {
		if (fields.size() == 0) {
			return null;
		}
		ensureFreshItem(rdfUrl, uri);
		String select = "CONSTRUCT { ";
		String where = "} WHERE { ";
		int cnt = 0;
		Map<String, String> varNameMap = new HashMap<String, String>();
		for (String field : fields) {
			String var = "o" + cnt++;
			String substring = "<" + uri + "> <" + field + "> ?" + var + " . ";
			select += substring;
			where += "OPTIONAL {" + substring + "} "; 			
			varNameMap.put(var, field);
		}
		return sparqlQueryClient.construct(select + where + "}");
	}
	
	public static void main(String[] args) {
		try  {		
			String dateTimeStr = "2015-07-28T15:28:13Z";
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
			System.out.println(format.format(Calendar.getInstance().getTime()));
			System.out.println(ISODateTimeFormat.dateTime().print(new DateTime()));
			System.out.println(R2ROntology.createDefaultModel().createTypedLiteral(Calendar.getInstance()).toString());
			System.out.println(R2ROntology.createDefaultModel().createTypedLiteral(Calendar.getInstance()).getLexicalForm());
			System.out.println(R2ROntology.createDefaultModel().createTypedLiteral(Calendar.getInstance()).getDatatypeURI());
			//System.out.println(
					//ISODateTimeFormat.dateTime().parseDateTime(dateTimeStr).toDateTime().plusHours(0)
					//);
			
			String fusekiURL = "http://localhost:3030/profiles";
			FusekiCache userCache = new FusekiCache(new SparqlQueryClient(fusekiURL + "/query"),
					new SparqlPostClient(fusekiURL + "/update", fusekiURL + "/data?default"), 
					null, null);//new DbService(systemDomain, orngUser, dbUtil));			// get these first
			userCache.getResource("http://stage-profiles.ucsf.edu/profiles260/profile/41510337");
//			SparqlQueryClient sq = new SparqlQueryClient("http://localhost:3030/profiles/query");
//			SparqlPostClient fs = new SparqlPostClient("http://localhost:3030/profiles/update", "http://localhost:3030/profiles/data?default");
//			FusekiCache fc = new FusekiCache(sq, fs, null);
//			fc.hasExpired("http://stage-profiles.ucsf.edu/profiles200/profile/366860");
//			fc.hasExpired("http://stage-profiles.ucsf.edu/profiles200/profile/123");
//			
//			Set<String> fields = new HashSet<String>();
//			fields.add("http://ucsf.edu/ontology/R2R#addedToCacheOn");
//			fields.add("http://www.w3.org/2000/01/rdf-schema#label");
//			fields.add("http://www.w3.org/2000/01/rdf-schema#foo");
//			Model model = fc.getModel("http://stage-profiles.ucsf.edu/profiles200/profile/365069", fields);
//			model.write(System.out);
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

	private class EmailEncryptedResultSetConsumer implements ResultSetConsumer {
		private String emailEncrypted;
		
		public void useResultSet(ResultSet rs) {
			while (rs.hasNext()) {
				QuerySolution qs = rs.next();
				if (qs.contains("?ee")) {
					emailEncrypted = qs.getLiteral("?ee").getString();
					break;
				}
			}				
		}	
		
		public String getEmailEncrypted() {
			return emailEncrypted;
		}		
	}
}
