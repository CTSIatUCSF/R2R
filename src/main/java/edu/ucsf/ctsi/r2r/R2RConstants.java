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
	static final String FOAF_PUBLICATIONS = FOAF + "publications";			
	static final String FOAF_HOMEPAGE = FOAF + "workInfoHomepage";			
	static final String FOAF_FIRST_NAME = FOAF + "firstName";			
	static final String FOAF_LAST_NAME = FOAF + "lastName";			
	static final String FOAF_IMAGE = FOAF + "Image";			
	static final String FOAF_HAS_IMAGE = FOAF + "img";			
	static final String FOAF_THUMBNAIL = FOAF + "thumbnail";			
	
	static final String BIBO_PREFIX = "bibo"; 
	static final String BIBO = "http://purl.org/ontology/bibo/";
	static final String BIBO_DOCUMENT = BIBO + "Document";
	static final String BIBO_ARTICLE = BIBO + "Article";
	static final String BIBO_PMID = BIBO + "pmid";
	static final String BIBO_DOI = BIBO + "doi";
	
	static final String GEO_PREFIX = "geo";
	static final String GEO = "http://www.w3.org/2003/01/geo/wgs84_pos#";
	static final String GEO_SPATIALTHING = GEO + "SpatialThing";
	static final String GEO_LATITUDE = GEO + "lat";
	static final String GEO_LONGITUDE = GEO + "long";

	static final String VIVO_PREFIX = "vivo";
	static final String VIVO = "http://vivoweb.org/ontology/core#";
	static final String VIVO_PMCID = VIVO + "pmcid";
	static final String VIVO_PREFERRED_TITLE = VIVO + "preferredTitle";			
	static final String VIVO_ORCID_ID = VIVO + "orcidId";

	static final String PRNS_PREFIX = "prns";
	static final String PRNS = "http://profiles.catalyst.harvard.edu/ontology/prns#";
//	static final String PRNS_LATITUDE = PRNS + "latitude";
//	static final String PRNS_LONGITUDE = PRNS + "longitude";
	
	static final String R2R_PREFIX = "r2r";
	static final String R2R = "http://ucsf.edu/ontology/r2r#";
	
	// graphs
	static final String R2R_THUMBNAIL_GRAPH = R2R + "Thumbnail";
	static final String R2R_DERIVED_GRAPH = R2R + "DerivedData";
	//classes
	static final String R2R_AFFILIATION = R2R + "Affiliation";
	static final String R2R_PROCESSOR = R2R + "Processor";
	static final String R2R_PROCESSOR_RUN = R2R + "ProcessorRun";
	// properties
	static final String R2R_ADDED_TO_CACHE = R2R + "addedToCacheOn";
	static final String R2R_PROCESSOR_START_DT = R2R + "processorStartDT";	
	static final String R2R_PROCESSOR_END_DT = R2R + "processorEndDT";	
	
	static final String R2R_HAS_AFFILIATION = R2R + "hasAffiliation";	
	static final String R2R_HAS_ICON = R2R + "hasIcon";	
	static final String R2R_PROCESSED = R2R + "processed";
	static final String R2R_PROCESSED_BY = R2R + "processedBy";
	static final String R2R_PROCESSED_ON = R2R + "processedOn";
	static final String R2R_EXTERNAL_COAUTHOR_CNT = R2R + "extCoauthorCnt";
	static final String R2R_SHARED_PUB_CNT = R2R + "sharedPubCnt";
}
