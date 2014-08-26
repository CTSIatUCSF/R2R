package edu.ucsf.ctsi.r2r.jena;

import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;


import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.inject.Inject;
import com.google.inject.name.Named;

// put content headers in FusekiService and make that an abstract class
public class FusekiHttpClient extends FusekiClient {

	private static final Logger LOG = Logger.getLogger(FusekiHttpClient.class.getName());
	
	private String fusekiPost = "http://localhost:3030/ds/data?default";
	private String fusekiUpdate = "http://localhost:3030/ds/update";
	
	@Inject
	public FusekiHttpClient(@Named("r2r.fuseki") String fusekiURL) {
		super(fusekiURL);
		this.fusekiPost = fusekiURL + "/data?default";
		this.fusekiUpdate = fusekiURL + "/update";
	}
			
	public int deleteSubject(String uri) throws Exception {
		return post(fusekiUpdate, UPDATE_CONTENT_TYPE, ("DELETE WHERE { <" + uri + ">  ?p ?o }").getBytes());
	}

	public int add(byte[] body) throws Exception {
		return post(fusekiPost, ADD_CONTENT_TYPE, body);
	}

	public int update(String sparql) throws Exception {
		return post(fusekiUpdate, UPDATE_CONTENT_TYPE, sparql.getBytes());
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
