package edu.ucsf.ctsi.r2r.jena;

import com.hp.hpl.jena.rdf.model.Model;

public interface ModelService {
	
	Model getModel(String uri) throws Exception;		
}
