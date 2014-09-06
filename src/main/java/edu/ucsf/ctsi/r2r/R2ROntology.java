package edu.ucsf.ctsi.r2r;

import java.io.FileWriter;
import java.io.IOException;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.XSD;


public class R2ROntology implements R2RConstants {
		
	public R2ROntology() {
	}
	
	public static Model createDefaultModel() {
		Model model = ModelFactory.createDefaultModel();
		addNS(model);
		return model;
	}
	
	public static void addNS(Model model) {
		model.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
		model.setNsPrefix(RDF_PREFIX, RDF);
		model.setNsPrefix(RDFS_PREFIX, RDFS);
		model.setNsPrefix(BIBO_PREFIX, BIBO);
		model.setNsPrefix(FOAF_PREFIX, FOAF);
		model.setNsPrefix(R2R_PREFIX, R2R);
		model.setNsPrefix(VIVO_PREFIX, VIVO);
		model.setNsPrefix(PRNS_PREFIX, PRNS);
	}
	
	public OntModel getR2ROntModel() {
		OntModel ontModel = ModelFactory.createOntologyModel();
		addNS(ontModel);
		ontModel.read("http://xmlns.com/foaf/spec/", "RDF/XML");
		//ontModel.read("http://vivoweb.org/files/vivo-isf-public-1.6.owl", "RDF/XML");

		OntClass person = ontModel.createClass( FOAF_PERSON );
		OntClass affiliation = ontModel.createClass(R2R_AFFILIATION);
				
		// anything can have this
		OntProperty ts = ontModel.createOntProperty( R2R_ADDED_TO_CACHE );
		ts.addRange( XSD.xlong );
		ontModel.createMaxCardinalityRestriction(null, ts, 1);

		// Affiliation properties
		DatatypeProperty cst = ontModel.createDatatypeProperty( R2R_CRAWL_START_DT );
		cst.addDomain( affiliation );
		cst.addRange( XSD.xlong );			
		ontModel.createMaxCardinalityRestriction(null, cst, 1);
				
		DatatypeProperty cet = ontModel.createDatatypeProperty( R2R_CRAWL_END_DT );
		cet.addDomain( affiliation );
		cet.addRange( XSD.xlong );			
		ontModel.createMaxCardinalityRestriction(null, cet, 1);

		DatatypeProperty lat = ontModel.createDatatypeProperty( PRNS_LATITUDE );
		lat.addDomain( affiliation );
		lat.addRange( XSD.xstring );			
		ontModel.createMaxCardinalityRestriction(null, lat, 1);

		DatatypeProperty lon = ontModel.createDatatypeProperty( PRNS_LONGITUDE );
		lon.addDomain( affiliation );
		lon.addRange( XSD.xstring );			
		ontModel.createMaxCardinalityRestriction(null, lon, 1);

		// Person properties
		ObjectProperty aff = ontModel.createObjectProperty( R2R_HAS_AFFILIATION );
		aff.addDomain( person );
		aff.addRange( affiliation );		
		ontModel.createMaxCardinalityRestriction(null, aff, 1);

		// where did we acquire this data?
		ObjectProperty hf = ontModel.createObjectProperty( R2R_HARVESTED_FROM );
		hf.addDomain( person );
		aff.addRange( affiliation );		
		ontModel.createMaxCardinalityRestriction(null, hf, 1);

		// when did we last see this person?
		DatatypeProperty rv = ontModel.createDatatypeProperty( R2R_VERIFIED_DT );
		rv.addDomain( person );
		rv.addRange( XSD.xlong );			
		ontModel.createMaxCardinalityRestriction(null, rv, 1);

		// when did we last see this persons work?
		DatatypeProperty wv = ontModel.createDatatypeProperty( R2R_WORK_VERIFIED_DT );
		wv.addDomain( person );
		wv.addRange( XSD.xlong );			
		ontModel.createMaxCardinalityRestriction(null, wv, 1);
	
		DatatypeProperty thumbnail = ontModel.createDatatypeProperty( R2R_THUMBNAIL );
		thumbnail.addDomain( person );
		thumbnail.addRange( XSD.anyURI);	
		ontModel.createMaxCardinalityRestriction(null, thumbnail, 1);

		ObjectProperty ab = ontModel.createObjectProperty( R2R_CONTRIBUTED_TO );
		ab.addDomain( person );
		ab.addRange( XSD.anyURI);
				
		ObjectProperty pu = ontModel.createObjectProperty( R2R_PRETTY_URL );
		pu.addDomain( person );
		pu.addRange( XSD.anyURI);

		return ontModel;
	}
	
	public static void printModel(String filename) throws IOException {
		R2ROntology r2r = new R2ROntology();
		OntModel m = r2r.getR2ROntModel();
		FileWriter out = null;
		try {
		  // XML format - long and verbose
		  out = new FileWriter( filename );
		  m.write( out, "RDF/XML-ABBREV" );
		}
		finally {
		  if (out != null) {
			  out.close();
		  }
		}				
	}
	
	public static void main(String[] args) {
		try {
			printModel(args.length > 0 ? args[0] : "R2R.owl");				
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}	
}
