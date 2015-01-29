package edu.ucsf.ctsi.r2r.jena;

import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.json.JSONException;
import org.json.JSONObject;

import com.hp.hpl.jena.rdf.model.Model;

public class JsonLDService {

	private static final Logger LOG = Logger.getLogger(JsonLDService.class.getName());

	// only use this if the client JSON can know this and take advantage of it easily
	private String base;
	
	public JsonLDService() {
		this(null);
	}
	
	public JsonLDService(String base) {
		this.base = base;
	}
	
	public String getBase() {
		return base;
	}
	
	public JSONObject getJSONObject(Model model) throws JSONException {
        String str = getJSONString(model);
        LOG.log(Level.FINE, str);
        return new JSONObject(str);
	}

	public String getJSONString(Model model) throws JSONException {
		StringWriter sw = new StringWriter();
		RDFDataMgr.write(sw, model, RDFFormat.JSONLD);
		return sw.toString();
	}

}
