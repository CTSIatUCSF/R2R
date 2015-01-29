package edu.ucsf.ctsi.r2r.jena;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

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

import edu.ucsf.ctsi.r2r.R2RConstants;

// put content headers in FusekiService and make that an abstract class
public class SparqlUpdateClient implements R2RConstants {

	private static final Logger LOG = Logger.getLogger(SparqlUpdateClient.class.getName());
	
	private String sparqlUpdate = "http://localhost:3030/ds/update";
	
	private static final ThreadLocal<AtomicInteger> txRequestCnt = new ThreadLocal<AtomicInteger>();
	private static final ThreadLocal<UpdateRequest> txRequest = new ThreadLocal<UpdateRequest>();
	
	private static final String DELETE_BNODE_OF_LABEL = "DELETE WHERE {<%1$s> <%2$s> ?bn . ?bn <" + RDFS_LABEL +
			"> \"%3$s\" . ?bn ?p ?o}";
			
	
	public SparqlUpdateClient(String sparqlUpdate) {
		this.sparqlUpdate = sparqlUpdate;
	}
			
	public void startTransaction() {
		if (txRequestCnt.get() == null) {
			txRequestCnt.set(new AtomicInteger());
			txRequest.set(UpdateFactory.create());
		}
		txRequestCnt.get().incrementAndGet();
	}

	public void endTransaction() {
		if (txRequestCnt.get().decrementAndGet() == 0) {
			execute(txRequest.get());
			txRequestCnt.remove();
			txRequest.remove();
		}
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
		// if any blank nodes are found, add those as well
		// but only go one level in, otherwise this will recurse forever!
		ArrayList<String> sparql = null;
		if (!resource.isAnon()) {
			sparql = new ArrayList<String>();
			StmtIterator si = resource.listProperties();
			while (si.hasNext()) {
				Statement s = si.next();
				if (s.getObject().isAnon()) {
					int blankCnt = 0;
					String spql = "INSERT DATA{ <" + resource.getURI() + "> <" + s.getPredicate().getURI() + 
							"> _:blank" + blankCnt + " . _:blank" + blankCnt++; 
					StmtIterator bsi = s.getObject().asResource().listProperties();
					while (bsi.hasNext()) {
						Statement bs = bsi.next();
						if (bs.getObject().isLiteral()) {
							if (RDFS_LABEL.equals(bs.getPredicate().getURI())) {
								sparql.add(0, String.format(DELETE_BNODE_OF_LABEL,
										resource.getURI(),
										s.getPredicate().getURI(), 
										bs.getObject().asLiteral().getLexicalForm()));
							}
							spql += " <" + bs.getPredicate().getURI() + "> \"" + 
									bs.getObject().asLiteral().getLexicalForm() + "\"^^<" + 
									bs.getObject().asLiteral().getDatatypeURI() + "> ; ";
						}
					}
					if (blankCnt > 0) {
						sparql.add(spql + "}");						
					}
				}
			}
		}
		return add(resource.listProperties(), resource.getModel(), sparql);
	}

	public int add(Model model) throws Exception {
		return add(model.listStatements(), model);
	}
	
	private int add(StmtIterator si, Model model) throws Exception {
		return add(si, model, null);
	}
	
	private int add(StmtIterator si, Model model, List<String> sparql) throws Exception {
		QuadDataAcc data = new QuadDataAcc();
		while (si.hasNext()) {
			Statement s = si.next();
			if (s.getObject().isAnon()) {
				continue;
			}
			Triple t = s.asTriple();
			data.addTriple(t);
		}
		Update updateData = new UpdateDataInsert(data);
		boolean execute = false;
		UpdateRequest tx = getCurrentTxRequest();
		if (tx == null) {
			tx = UpdateFactory.create();
			execute = true;
		}
		tx.add(updateData);
		if (sparql != null) {
			for (String spql : sparql) {
				tx.add(spql);					
			}
		}
		tx.setPrefixMapping(model);
		if (execute) {
			execute(tx);
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
