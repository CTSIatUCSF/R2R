package edu.ucsf.ctsi.r2r.jena;

import org.apache.jena.rdf.model.Model;

public interface ModelService {
	
	Model getModel(String uri) throws Exception;		
}
