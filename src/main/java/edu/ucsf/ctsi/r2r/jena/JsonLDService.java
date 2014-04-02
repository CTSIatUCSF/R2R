package edu.ucsf.ctsi.r2r.jena;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.jena.JenaRDFParser;
import com.github.jsonldjava.utils.JSONUtils;
import com.hp.hpl.jena.rdf.model.Model;

public class JsonLDService {

	private static final Logger LOG = Logger.getLogger(JsonLDService.class.getName());

	private static final String RDFXML = "application/rdf+xml";

	// only use this if the client JSON can know this and take advantage of it easily
	private String base;
	
	public JsonLDService() {
		this(null);
	}
	
	public JsonLDService(String base) {
		this.base = base;
    	JsonLdProcessor.registerRDFParser(RDFXML, new JenaRDFParser());
	}
	
	public String getBase() {
		return base;
	}
	
	public JSONObject getJSONObject(Model model) throws JSONException, JsonLdError {
        String str = getJSONString(model);
        LOG.log(Level.FINE, str);
        return new JSONObject(str);
	}

	public String getJSONString(Model model) throws JSONException, JsonLdError {
        final JsonLdOptions opts = base != null ? new JsonLdOptions(base) : new JsonLdOptions();
        opts.format = RDFXML;
        // maybe have outputForm be configurable?
        opts.outputForm = "compacted";
    	// do we need to simplify?
    	Object obj = JsonLdProcessor.fromRDF(model, opts); 
        return JSONUtils.toString(obj);
	}
}
