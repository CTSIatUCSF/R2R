package edu.ucsf.ctsi.r2r.jena;

import java.io.ByteArrayInputStream;
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
public class FusekiResourceCache implements ResourceService {

	private static final String TIMESTAMP = "http://ucsf.edu/ontology/R2R#addedToCacheOn";
	private static final Logger LOG = Logger.getLogger(FusekiResourceCache.class.getName());
	
	private FusekiService fusekiService;
	private RDFXMLService rdfxmlService;
	private int cacheExpire;
	
	
	@Inject
	public FusekiResourceCache(FusekiService fusekiService, RDFXMLService rdfxmlService, String cacheExpire) {
		this.fusekiService = fusekiService;
		this.rdfxmlService = rdfxmlService;
    	this.cacheExpire = Integer.parseInt(cacheExpire);
	}

	private Property createTimestamp(Model model) {
		return  model.createProperty(TIMESTAMP);
	}
		
	//check response HTTP codes at some point
	public Resource getResource(String uri) throws Exception {
		// see if we have a recent copy of this
		DateTime now = new DateTime();
		Model model = fusekiService.get(uri);
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
				return resource;
			}
		}
		
		// if we are here, we do not have this uri or we do not have a timestamped version of it
		// load the data
		byte[] body = rdfxmlService.getRDFXML(uri);
		if (body != null) {
			fusekiService.add(body);
			// now add the timestamp
			fusekiService.update("INSERT DATA { <" + uri + "> <" + TIMESTAMP + "> " + now.getMillis() + ".}");
		}
		
		return ModelFactory.createDefaultModel().read(new ByteArrayInputStream(body), null).createResource(uri);
	}

		
}
