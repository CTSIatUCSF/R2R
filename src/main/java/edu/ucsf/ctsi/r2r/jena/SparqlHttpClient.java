package edu.ucsf.ctsi.r2r.jena;

import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class SparqlHttpClient extends SparqlPostClient {

	
//	private static final String UPDATE_CONTENT_TYPE = "application/sparql-update";
	private static final String ADD_CONTENT_TYPE = "application/rdf+xml";

	private static final Logger LOG = Logger.getLogger(SparqlHttpClient.class.getName());
	
	private String sparqlPost = "http://localhost:3030/ds/data?default";
	
	@Inject
	public SparqlHttpClient(@Named("r2r.fusekiUrl") String fusekiURL) {
		this(fusekiURL + "/sparql", fusekiURL + "/update", fusekiURL + "/data?default");
	}

	public SparqlHttpClient(String query, String update, String post) {
		super(query, update);
		this.sparqlPost = post;
	}

	public int add(byte[] body) throws Exception {
		return post(sparqlPost, ADD_CONTENT_TYPE, body);
	}

	private int post(String url, String contentType, byte[] body) throws Exception {
		// HttpClient httpclient = HttpClientBuilder.create().build(); this works with httpclient 4.3.3, but not 4.2.5
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httpost = new HttpPost(url);
		httpost.addHeader("content-type", contentType);
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
