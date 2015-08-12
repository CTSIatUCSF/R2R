package edu.ucsf.ctsi.r2r.jena;

import java.io.ByteArrayOutputStream;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.jena.rdf.model.Model;

public class SparqlPostClient extends SparqlUpdateClient {
	
	private static final Logger LOG = Logger.getLogger(SparqlPostClient.class.getName());
	
	protected static final String UPDATE_CONTENT_TYPE = "application/sparql-update";
	protected static final String ADD_CONTENT_TYPE = "application/rdf+xml";

//	this(fusekiURL + "/sparql", fusekiURL + "/update", fusekiURL + "/data?default");
	private String sparqlPost = "http://localhost:3030/ds/data?default";

	public SparqlPostClient(String sparqlUpdate, String sparqlPost) {
		super(sparqlUpdate);
		this.sparqlPost = sparqlPost;
	}
	
	protected String getSparqlPostEndpoint() {
		return sparqlPost;
	}

	public int post(Model model) throws Exception {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		model.write(stream);
		stream.flush();
		stream.close();
		return add(stream.toByteArray());
	}

	public int add(byte[] body) throws Exception {
		// HttpClient httpclient = HttpClientBuilder.create().build(); this works with httpclient 4.3.3, but not 4.2.5
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httpost = new HttpPost(getSparqlPostEndpoint());
		httpost.addHeader("content-type", ADD_CONTENT_TYPE);
		httpost.setEntity(new ByteArrayEntity(body));
		HttpResponse response = httpclient.execute(httpost);
		return response.getStatusLine().getStatusCode();
//		Response response = Request.Post(url)
//				.bodyByteArray(body)
//				.addHeader("content-type", contentType)
//				.execute();
//		return response.returnResponse().getStatusLine().getStatusCode();		
	}
}
