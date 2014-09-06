package edu.ucsf.ctsi.r2r.jena;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.modify.request.QuadDataAcc;
import com.hp.hpl.jena.sparql.modify.request.UpdateDataInsert;
import com.hp.hpl.jena.update.Update;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

// put content headers in FusekiService and make that an abstract class
public class SparqlUpdateClient extends SparqlClient {

	private static final Logger LOG = Logger.getLogger(SparqlUpdateClient.class.getName());
	
	private String sparqlUpdate = "http://localhost:3030/ds/update";
	
	private static final ThreadLocal<UpdateRequest> txRequest = new ThreadLocal<UpdateRequest>();
	
	@Inject
	public SparqlUpdateClient(@Named("r2r.fusekiUrl") String fusekiURL) {
		this(fusekiURL + "/sparql", fusekiURL + "/update");
	}
			
	public SparqlUpdateClient(String query, String update) {
		super(query);
		this.sparqlUpdate = update;
	}
			
	public void startTransaction() {
		txRequest.set(UpdateFactory.create());
	}

	public void endTransaction() {
		execute(txRequest.get());
		txRequest.remove();
	}
	
	private UpdateRequest getCurrentTxRequest() {
		return txRequest.get();
	}
	
	private void execute(UpdateRequest request) {
		UpdateProcessor processor = UpdateExecutionFactory.createRemote(request, sparqlUpdate);
		processor.execute();		
	}
	
	public int deleteSubject(String uri) throws Exception {
		return update("DELETE WHERE { <" + uri + ">  ?p ?o }");
	}

	public int add(Resource resource) throws Exception {
		return add(resource.listProperties(), resource.getModel());
	}

	public int add(Model model) throws Exception {
		return add(model.listStatements(), model);
	}
	
	private int add(StmtIterator si, Model model) throws Exception {
		QuadDataAcc data = new QuadDataAcc();
		while (si.hasNext()) {
			Statement s = si.next();
			Triple t = s.asTriple();
			data.addTriple(t);
		}
		Update updateData = new UpdateDataInsert(data);
		UpdateRequest tx = getCurrentTxRequest();
		if (tx != null) {
			tx.add(updateData);
			tx.setPrefixMapping(model);
		}
		else {
			UpdateRequest request = UpdateFactory.create();
			request.add(updateData);
			request.setPrefixMapping(model);
			execute(request);
		}
		return 0;
	}

	public int update(List<String> sparql) throws Exception {
		//return post(fusekiUpdate, UPDATE_CONTENT_TYPE, sparql.getBytes());
		UpdateRequest tx = getCurrentTxRequest();
		if (tx != null) {
			for (String s : sparql) {			
				LOG.info("UPDATE BATCH :" + s);
				tx.add(s);
			}			
		}
		else {
			UpdateRequest request = UpdateFactory.create();
			for (String s : sparql) {			
				LOG.info("UPDATE BATCH :" + s);
				request.add(s);
			}
			execute(request);
		}
		return 0;
	}

	public int update(String sparql) throws Exception {
		return update(Arrays.asList(sparql));
	}

}
