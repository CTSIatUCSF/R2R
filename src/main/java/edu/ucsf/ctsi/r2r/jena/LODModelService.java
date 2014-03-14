package edu.ucsf.ctsi.r2r.jena;

import java.net.URLEncoder;
import java.util.logging.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;

public class LODModelService implements ModelService {

	private static final Logger LOG = Logger.getLogger(LODModelService.class.getName());
	
	private String systemDomain;
	private String systemBase;
	private String sessionId;
	private String viewerId;
	private boolean showDetails = true;
	private boolean expand = false;
	
	@Inject
	public LODModelService(@Named("orng.systemDomain") String systemDomain,
						  String sessionId, String viewerId) {
		this.systemDomain = systemDomain;
		this.systemBase = systemDomain + "/profile/";
		this.sessionId = sessionId;
		this.viewerId = viewerId;
	}

	public void setProfilesOptions(boolean showDetails, boolean expand) {
		this.showDetails = showDetails;
		this.expand = expand;
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

}
