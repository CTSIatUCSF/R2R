package edu.ucsf.ctsi.r2r.jena;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;


// make this an interface that can work without httpfetcher
public final class SparqlQueryClient {
	
	private static final Logger LOG = Logger.getLogger(SparqlQueryClient.class.getName());
	
	private String endpoint = null;
	
	private long readCnt = 0;
	private long timeout1 = -1;
	private long timeout2 = -1;

	public SparqlQueryClient(String endpoint, long timeout1, long timeout2) {
		this.endpoint = endpoint;
		this.timeout1 = timeout1;
		this.timeout2 = timeout2;
	}

	public SparqlQueryClient(String endpoint) {
		this(endpoint, -1, -1);
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
	
	public void select(String sparql, ResultSetConsumer consumer) throws Exception {
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

	private QueryExecution getQueryExecution(String sparql) {
		QueryExecution qe = QueryExecutionFactory.sparqlService(endpoint, sparql);
		qe.setTimeout(timeout1, timeout2);
		LOG.info("Timeout = " + qe.getTimeout1() + "," + qe.getTimeout2() + " : readTxCount = " + readCnt++ + " for " + sparql);
		return qe;
	}

}
