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
	
	OntModel foaf;
	
	public R2ROntology() {
		// read in foaf
		foaf = ModelFactory.createOntologyModel();
		foaf.read("http://xmlns.com/foaf/spec/", "RDF/XML");
	}
	
	public static Model createDefaultModel() {
		Model model = ModelFactory.createDefaultModel();
		addNS(model);
		return model;
	}
	
	public static void addNS(Model model) {
		model.setNsPrefix(RDF_PREFIX, RDF);
		model.setNsPrefix(RDFS_PREFIX, RDFS);
		model.setNsPrefix(BIBO_PREFIX, BIBO);
		model.setNsPrefix(FOAF_PREFIX, FOAF);
		model.setNsPrefix(VIVO_PREFIX, VIVO);
		model.setNsPrefix(PRNS_PREFIX, PRNS);
		model.setNsPrefix(R2R_PREFIX, R2R);
	}
	
	public OntModel getR2ROntModel() {
		OntModel m = ModelFactory.createOntologyModel();
		addNS(m);

		Ontology ont = m.createOntology( R2R );
		ont.addImport( m.createResource( FOAF ) );
		OntClass person = foaf.getOntClass( FOAF_PERSON );
		OntClass affiliation = m.createClass(R2R_RN_WEBSITE);
				
		// anything can have this
		OntProperty ts = m.createOntProperty( R2R_ADDED_TO_CACHE );
		ts.addRange( XSD.xlong );
		m.createMaxCardinalityRestriction(null, ts, 1);

		// Affiliation properties
		DatatypeProperty cst = m.createDatatypeProperty( R2R_CRAWL_START_DT );
		cst.addDomain( affiliation );
		cst.addRange( XSD.xlong );			
		m.createMaxCardinalityRestriction(null, cst, 1);
				
		DatatypeProperty cet = m.createDatatypeProperty( R2R_CRAWL_END_DT );
		cet.addDomain( affiliation );
		cet.addRange( XSD.xlong );			
		m.createMaxCardinalityRestriction(null, cet, 1);

		// Person properties
		ObjectProperty aff = m.createObjectProperty( R2R_FROM_RN_WEBSITE );
		aff.addDomain( person );
		aff.addRange( affiliation );		
		m.createMaxCardinalityRestriction(null, aff, 1);

		// when did we last see this person?
		DatatypeProperty rv = m.createDatatypeProperty( R2R_VERIFIED_DT );
		rv.addDomain( person );
		rv.addRange( XSD.xlong );			
		m.createMaxCardinalityRestriction(null, rv, 1);

		// when did we last see this persons work?
		DatatypeProperty wv = m.createDatatypeProperty( R2R_WORK_VERIFIED_DT );
		wv.addDomain( person );
		wv.addRange( XSD.xlong );			
		m.createMaxCardinalityRestriction(null, wv, 1);
	
		DatatypeProperty thumbnail = m.createDatatypeProperty( R2R_THUMBNAIL );
		thumbnail.addDomain( person );
		thumbnail.addRange( XSD.anyURI);	
		m.createMaxCardinalityRestriction(null, thumbnail, 1);

		ObjectProperty ab = m.createObjectProperty( R2R_CONTRIBUTED_TO );
		ab.addDomain( person );
				
		return m;
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
