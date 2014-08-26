package edu.ucsf.ctsi.r2r.jena;

import java.io.ByteArrayOutputStream;
import java.util.logging.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.rdf.model.Model;

// make this an interface that can work without httpfetcher
public abstract class FusekiClient {
	
	protected static final String UPDATE_CONTENT_TYPE = "application/sparql-update";
	protected static final String ADD_CONTENT_TYPE = "application/rdf+xml";

	private static final Logger LOG = Logger.getLogger(FusekiClient.class.getName());
	
	private String fusekiQuery = "http://localhost:3030/ds/query";

	@Inject
	public FusekiClient(@Named("r2r.fuseki") String fusekiURL) {
		this.fusekiQuery = fusekiURL + "/query";
	}

	public Model describe(String uri) {
		QueryExecution qe = getQueryExecution("DESCRIBE <" + uri + ">");
		try {
			return qe.execDescribe();
		}
		finally {
			qe.close();			
		}
	}
	
	public boolean ask(String sparql) {
		QueryExecution qe = getQueryExecution(sparql);
		try {
			return qe.execAsk();
		}
		finally {
			qe.close();			
		}
	}
	
	public void select(String sparql, ResultSetConsumer consumer) {
		QueryExecution qe = getQueryExecution(sparql);
		try {
			consumer.useResultSet(qe.execSelect());
		}
		finally {
			qe.close();			
		}
	}

	public Model construct(String sparql) {
		QueryExecution qe = getQueryExecution(sparql);
		try {
			return qe.execConstruct();
		}
		finally {
			qe.close();			
		}
	}

	public int add(Model model) throws Exception {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		model.write(stream);
		stream.flush();
		stream.close();
		return add(stream.toByteArray());
	}
	
	private QueryExecution getQueryExecution(String sparql) {
		QueryExecution qe = QueryExecutionFactory.sparqlService(fusekiQuery, sparql);
		LOG.info("Timeout = " + qe.getTimeout1() + "," + qe.getTimeout2() + " for " + sparql);
		return qe;
	}
	
	// this will only delete the URI as a subject!	
	public abstract int deleteSubject(String uri) throws Exception;

	public abstract int add(byte[] body) throws Exception;

	public abstract int update(String sparql) throws Exception;
}
