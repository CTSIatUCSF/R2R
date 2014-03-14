package edu.ucsf.ctsi.r2r.jena;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.joda.time.Period;

import com.google.inject.Inject;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import edu.ucsf.ctsi.r2r.jena.ResourceService;
import edu.ucsf.ctsi.r2r.jena.RDFXMLService;

// put this in r2r jar to be shared with Crosslinks, add something about storing it or not
public class FusekiCache implements ModelService, RDFXMLService, ResourceService {

	private static final String TIMESTAMP = "http://ucsf.edu/ontology/R2R#addedToCacheOn";
	private static final Logger LOG = Logger.getLogger(FusekiCache.class.getName());
	
	private FusekiService fusekiService;
	private RDFXMLService rdfxmlService;
	private int cacheExpire;
		
	@Inject
	public FusekiCache(FusekiService fusekiService, RDFXMLService rdfxmlService, String cacheExpire) {
		this.fusekiService = fusekiService;
		this.rdfxmlService = rdfxmlService;
    	this.cacheExpire = Integer.parseInt(cacheExpire);
	}

	private Property createTimestamp(Model model) {
		return  model.createProperty(TIMESTAMP);
	}
	
	public boolean contains(String uri) {
		Model model = fusekiService.get(uri);
		return model.contains(ResourceFactory.createResource(uri), null);		
	}
	
	// we return either a Model or a byte[] because its more efficient to let clients transform if and when necessary
	private Object getFreshItem(String rdfUrl, String uri) throws Exception  {
		// see if we have a recent copy of this
		DateTime now = new DateTime();
		Model model = fusekiService.get(rdfUrl);
		if (model.contains(ResourceFactory.createResource(uri), null)) {
	        Resource resource = model.createResource(uri);
	        boolean resourceExpired = true;
			if (resource.hasProperty(createTimestamp(model)) ) {
				Literal writeTime = resource.getProperty(createTimestamp(model)).getLiteral();
				resourceExpired = new Period(new DateTime(writeTime.getLong()), now).getHours() > cacheExpire;
			}
			
			if (resourceExpired) {
				// remove it!
				fusekiService.delete(uri);
	        	LOG.info("Expired " + uri);
			}	
			else {
				return model;
			}
		}		
		try {
			// if we get here, we need to add to cache
			byte[] body = rdfxmlService.getRDFXML(rdfUrl);
			if (body != null) {
				fusekiService.add(body);
				// now add the timestamp
				fusekiService.update("INSERT DATA { <" + uri + "> <" + TIMESTAMP + "> " + now.getMillis() + ".}");
			}
			return body;
		}
		catch (Exception e) {
			LOG.log(Level.WARNING, "Could not access RDF from " + uri, e);
		}
		return null;
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
}
