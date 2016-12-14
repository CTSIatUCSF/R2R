package edu.ucsf.ctsi.r2r.jena;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;
import java.util.logging.Logger;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileManager;

import edu.ucsf.ctsi.r2r.R2ROntology;


public class LODService implements ModelService, RDFXMLService, ResourceService {

	private static final Logger LOG = Logger.getLogger(LODService.class.getName());
	
	private String systemDomain;
	private String systemBase;
	private String sessionId;
	private String viewerId;
	private boolean showDetails = true;
	private boolean expand = false;
	
	public LODService(String systemDomain, String sessionId, String viewerId, boolean showDetails, boolean expand) {
		this.systemDomain = systemDomain;
		this.systemBase = systemDomain + "/profile/";
		this.sessionId = sessionId;
		this.viewerId = viewerId;
		this.showDetails = showDetails;
		this.expand = expand;
	}
	
	public LODService() {
		this("", null, null, true, false);
	}
	
	public byte[] getRDFXML(String uri) throws Exception {
		Model model = getModel(uri);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		model.write(stream);
		stream.flush();
		stream.close();
		return stream.toByteArray();
	}
	
	public Model getModel(String uri) throws Exception {	
		String url = uri;
		Integer nodeId = getNodeId(uri);
		if (nodeId != null) {
			url = systemDomain + "/Profile/Profile.aspx?Subject=" + nodeId;
			// add in SessionID so that we can take advantage of Profiles security settings
			url += "&ShowDetails=" + showDetails + "&Expand=" + expand;
			if (sessionId != null)
			{
				url += "&ContainerSessionID=" + sessionId;					
			}
			if (viewerId != null)
			{
				url += "&Viewer=" + URLEncoder.encode(viewerId, "UTF-8");					
			}
		}		
		LOG.info("Loading : " + url);

    	return FileManager.get().loadModel(url);			
	}

	private Integer getNodeId(String uri) {
		if (uri.toLowerCase().startsWith(systemBase)) {
			return Integer.parseInt(uri.split(systemBase)[1]);
		}
		return null;
	}

	public Resource getResource(String uri) throws Exception {
		return getModel(uri).createResource(uri);
	}
	
	public static void main(String[] args) {
		try {			
			String out = new Scanner(new URL(args[0]).openStream(), "UTF-8").useDelimiter("\\A").next();
			System.out.println(out);
			System.out.println("+++++++++++++++++++++++++++ MODEL FROM ABOVE +++++++++++++++++++++++++++++");
			Model model = R2ROntology.createDefaultModel().read(new ByteArrayInputStream(out.getBytes()), null);		
			model.write(System.out);
			System.out.println("+++++++++++++++++++++++++++ +++++++++++++++++++++++++++++");
			
			model = FileManager.get().loadModel(args[0]);
			model.write(System.out);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
