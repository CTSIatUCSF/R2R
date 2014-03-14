package edu.ucsf.ctsi.r2r;

public interface R2RConstants {
	static final String SOURCE = "http://ucsf.edu/ontology/R2R";
	static final String NS = SOURCE + "#";			
	static final String ADDED_TO_CACHE = NS + "addedToCacheOn";
	
	static final String LABEL = "http://www.w3.org/2000/01/rdf-schema#label";
	static final String TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
	
	static final String FOAF_PREFIX = "foaf";
	static final String FOAF = "http://xmlns.com/foaf/0.1/";
	static final String HOMEPAGE = FOAF + "homepage";			
	static final String IMAGE = FOAF + "img";	
}
