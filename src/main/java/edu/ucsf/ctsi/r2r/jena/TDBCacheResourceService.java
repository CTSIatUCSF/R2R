package edu.ucsf.ctsi.r2r.jena;

import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.joda.time.Period;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.tdb.TDBFactory;

public class TDBCacheResourceService implements ResourceService {

	private static final Logger LOG = Logger.getLogger(TDBCacheResourceService.class.getName());
	
	private ModelService modelService;
	private String tdbBaseDir;
	private int cacheExpire;
	private String systemBase;
	
	@Inject
	public TDBCacheResourceService(@Named("orng.systemDomain") String systemDomain, 
			String tdbBaseDir, @Named("orng.tdbCacheExpireHours") String tdbCacheExpire,  ModelService modelService) {
    	this.tdbBaseDir = tdbBaseDir;
    	this.cacheExpire = Integer.parseInt(tdbCacheExpire);
    	this.modelService = modelService;
		this.systemBase = systemDomain + "/profile/";
	}
	
	private Dataset getDataset(ReadWrite readWrite) {
		Dataset dataset = TDBFactory.createDataset(tdbBaseDir);
		dataset.begin(readWrite);
		return dataset;
	}
	
	private boolean contains(String uri) {
    	Dataset dataSet = getDataset(ReadWrite.READ);
    	try {
    		Model model = dataSet.getDefaultModel();
	    	return model.contains(ResourceFactory.createResource(uri), null);
        }	
	    finally {
	    	dataSet.end();
	    }
	}
	
	private Property createTimestamp(Model model) {
		return  model.createProperty("http://orng.info/ontology/orng#addedToCacheOn");
	}
	
	public Resource getResource(String uri) throws Exception {
		Resource resource = getIfNotExpired(uri);
		if (resource == null) {
	    	Dataset dataSet = getDataset(ReadWrite.READ);
			Model readModel = dataSet.getDefaultModel();
	    	if (!readModel.contains(ResourceFactory.createResource(uri), null)) {
	            dataSet.end();
	            dataSet = getDataset(ReadWrite.WRITE);
	            Model writeModel = dataSet.getDefaultModel();
	            Model newModel = modelService.getModel(uri);
	            if (newModel != null) {
	            	writeModel.add(newModel);
	            }
	        	// put in timestamp
	        	resource = writeModel.createResource(uri);
	        	Property prop = createTimestamp(writeModel);
	        	writeModel.add(resource, prop, writeModel.createTypedLiteral(new DateTime().getMillis()));        	
	        	dataSet.commit();
	        	dataSet.end();
	        	LOG.info("Added  " + uri);
	        	dataSet = getDataset(ReadWrite.READ);
	    	}
	    	
	        resource = dataSet.getDefaultModel().createResource(uri);
	        dataSet.end();
		}
		return resource;
	}
	
	// need to make this smarter!
	protected Integer getNodeID(String uri) {
		if (uri.toLowerCase().startsWith(systemBase)) {
			return Integer.parseInt(uri.split(systemBase)[1]);
		}
		return null;
	}
		
	private Resource getIfNotExpired(String uri) {
		DateTime now = new DateTime();
		if (contains(uri)) {
	    	Dataset dataSet = getDataset(ReadWrite.READ);
            Model readModel =  dataSet.getDefaultModel();
            Resource resource = readModel.createResource(uri);
			if (resource.hasProperty(createTimestamp(readModel)) ) {
				Literal writeTime = resource.getProperty(createTimestamp(readModel)).getLiteral();
				if (new Period(new DateTime(writeTime.getLong()), now).getHours() > cacheExpire) {
		        	dataSet = getDataset(ReadWrite.WRITE);
		            dataSet.getDefaultModel().removeAll(resource, null, null);
		        	dataSet.commit();
		        	dataSet.end();
		        	LOG.info("Expired " + uri);
				}	
				else {
					return resource;
				}
			}
		}
		return null;
	}
}
