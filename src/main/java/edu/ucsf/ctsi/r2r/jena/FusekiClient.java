package edu.ucsf.ctsi.r2r.jena;

import java.io.ByteArrayOutputStream;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

// make this an interface that can work without httpfetcher
public abstract class FusekiClient {
	
	protected static final String UPDATE_CONTENT_TYPE = "application/sparql-update";
	protected static final String ADD_CONTENT_TYPE = "application/rdf+xml";

	private String fusekiQuery = "http://localhost:3030/ds/query";

	@Inject
	public FusekiClient(@Named("r2r.fuseki") String fusekiURL) {
		this.fusekiQuery = fusekiURL + "/query";
	}

	public Model describe(String uri) {
		QueryExecution qe = QueryExecutionFactory.sparqlService(fusekiQuery, "DESCRIBE <" + uri + ">");
		Model model = qe.execDescribe();
		qe.close();
		return model;
	}
	
	public boolean ask(String sparql) {
		QueryExecution qe = QueryExecutionFactory.sparqlService(fusekiQuery, sparql);
		boolean retval = qe.execAsk();
		qe.close();
		return retval;
	}
	
	public void select(String sparql, ResultSetConsumer consumer) {
		QueryExecution qe = QueryExecutionFactory.sparqlService(fusekiQuery, sparql);
		consumer.useResultSet(qe.execSelect());
		qe.close();
	}

	public Model construct(String sparql) {
		QueryExecution qe = QueryExecutionFactory.sparqlService(fusekiQuery, sparql);
		Model model = qe.execConstruct();
		qe.close();
		return model;
	}

	public int add(Model model) throws Exception {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		model.write(stream);
		stream.flush();
		stream.close();
		return add(stream.toByteArray());
	}
	
	// this will only delete the URI as a subject!
	
	public abstract int deleteSubject(String uri) throws Exception;

	public abstract int add(byte[] body) throws Exception;

	public abstract int update(String sparql) throws Exception;
}
