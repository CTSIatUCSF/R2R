package edu.ucsf.ctsi.r2r.jena;

import com.hp.hpl.jena.rdf.model.Resource;

public interface ResourceService {
	
	Resource getResource(String uri) throws Exception;

}
