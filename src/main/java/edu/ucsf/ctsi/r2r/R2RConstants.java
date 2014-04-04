package edu.ucsf.ctsi.r2r;

public interface R2RConstants {

	static final String RDF_PREFIX = "rdf"; 
	static final String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"; 
	static final String RDF_TYPE = RDF + "type";
	
	static final String RDFS_PREFIX = "rdfs";
	static final String RDFS = "http://www.w3.org/2000/01/rdf-schema#";	
	static final String RDFS_LABEL = RDFS + "label";
	
	static final String FOAF_PREFIX = "foaf";
	static final String FOAF = "http://xmlns.com/foaf/0.1/";
	static final String FOAF_PERSON = FOAF + "Person";			
	static final String FOAF_HOMEPAGE = FOAF + "homepage";		
	
	static final String BIBO_PREFIX = "bibo"; 
	static final String BIBO = "http://purl.org/ontology/bibo/";
	static final String BIBO_DOCUMENT = BIBO + "Document";
	static final String BIBO_ARTICLE = BIBO + "Article";
	static final String BIBO_PMID = BIBO + "pmid";
	static final String BIBO_DOI = BIBO + "doi";

	static final String VIVO_PREFIX = "vivo";
	static final String VIVO = "http://vivoweb.org/ontology/core#";
	static final String VIVO_PMCID = VIVO + "pmcid";
	static final String VIVO_ORCID_ID = VIVO + "orcidId";

	static final String PRNS_PREFIX = "prns";
	static final String PRNS = "http://profiles.catalyst.harvard.edu/ontology/prns#";
	static final String PRNS_MAIN_IMAGE = PRNS + "mainImage";
	static final String PRNS_LATITUDE = PRNS + "latitude";
	static final String PRNS_LONGITUDE = PRNS + "longitude";
	
	static final String R2R_PREFIX = "r2r";
	static final String R2R = "http://ucsf.edu/ontology/r2r#";
	//classes
	static final String R2R_RN_WEBSITE = R2R + "RNWebSite";
	// properties
	static final String R2R_ADDED_TO_CACHE = R2R + "addedToCacheOn";
	static final String R2R_THUMBNAIL = R2R + "thumbnail";
	static final String R2R_CRAWL_START_DT = R2R + "crawlStartDT";	
	static final String R2R_CRAWL_END_DT = R2R + "crawlEndDT";	
	static final String R2R_FROM_RN_WEBSITE = R2R + "fromRNWebSite";	
	static final String R2R_VERIFIED_DT = R2R + "verifiedDT";
	static final String R2R_WORK_VERIFIED_DT = R2R + "workVerifiedDT";
	static final String R2R_CONTRIBUTED_TO = R2R + "contributedTo";
}
