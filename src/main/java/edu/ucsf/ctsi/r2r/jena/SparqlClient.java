package edu.ucsf.ctsi.r2r.jena;

import java.util.logging.Logger;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.rdf.model.Model;

// make this an interface that can work without httpfetcher
public class SparqlClient {
	
	private static final Logger LOG = Logger.getLogger(SparqlClient.class.getName());
	
	private String endpoint = "http://marengo.info-science.uiowa.edu:2020/sparql";
	
	private long readCnt = 0;

	public SparqlClient(String endpoint) {
		this.endpoint = endpoint;
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
		QueryExecution qe = getQueryExecution(sparql, 1000, 1000);
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
		return getQueryExecution(sparql, -1, -1);
	}

	private QueryExecution getQueryExecution(String sparql, long timeout1, long timeout2) {
		QueryExecution qe = QueryExecutionFactory.sparqlService(endpoint, sparql);
		qe.setTimeout(timeout1, timeout2);
		LOG.info("Timeout = " + qe.getTimeout1() + "," + qe.getTimeout2() + " : readTxCount = " + readCnt++ + " for " + sparql);
		return qe;
	}

}
