package edu.ucsf.ctsi.r2r.jena;

import com.hp.hpl.jena.rdf.model.Model;

// make this an interface that can work without httpfetcher
public interface FusekiService {

	Model get(String uri);
	
	int delete(String uri) throws Exception;

	int add(byte[] body) throws Exception;

	int update(String sparql) throws Exception;
}
