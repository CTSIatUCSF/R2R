package edu.ucsf.ctsi.r2r;

public interface R2RConstants {
	static final String SOURCE = "http://ucsf.edu/ontology/R2R";
	static final String NS = SOURCE + "#";			
	static final String ADDED_TO_CACHE = NS + "addedToCacheOn";
	
	static final String TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

	static final String RDFS_PREFIX = "rdfs";
	static final String RDFS = "http://www.w3.org/2000/01/rdf-schema#";
	
	static final String LABEL = RDFS + "label";
	
	static final String FOAF_PREFIX = "foaf";
	static final String FOAF = "http://xmlns.com/foaf/0.1/";
	static final String HOMEPAGE = FOAF + "homepage";			
	static final String IMAGE = FOAF + "img";	

	static final String VIVO_PREFIX = "vivo";
	static final String VIVO = "http://vivoweb.org/ontology/core#";

	static final String PRNS_PREFIX = "prns";
	static final String PRNS = "http://profiles.catalyst.harvard.edu/ontology/prns#";
}
