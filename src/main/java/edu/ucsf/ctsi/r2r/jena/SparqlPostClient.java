package edu.ucsf.ctsi.r2r.jena;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public abstract class SparqlPostClient extends SparqlUpdateClient {
	
	protected static final String UPDATE_CONTENT_TYPE = "application/sparql-update";
	protected static final String ADD_CONTENT_TYPE = "application/rdf+xml";

	@Inject
	public SparqlPostClient(@Named("r2r.fusekiUrl") String fusekiURL) {
		this(fusekiURL + "/sparql", fusekiURL + "/update");
	}

	public SparqlPostClient(String query, String update) {
		super(query, update);
	}

	public abstract int add(byte[] body) throws Exception;
}
