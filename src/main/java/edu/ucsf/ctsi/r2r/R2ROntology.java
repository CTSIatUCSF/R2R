package edu.ucsf.ctsi.r2r;

import java.io.FileWriter;
import java.io.IOException;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.XSD;

public class R2ROntology implements R2RConstants {
	
	OntModel foaf;
	
	public R2ROntology() {
		// read in foaf
		foaf = ModelFactory.createOntologyModel();
		foaf.read("http://xmlns.com/foaf/spec/", "RDF/XML");
	}
			
	public OntModel getR2ROntModel() {
		OntModel m = ModelFactory.createOntologyModel();

    	m.setNsPrefix(FOAF_PREFIX, FOAF);
    	m.setNsPrefix(VIVO_PREFIX, VIVO);
    	m.setNsPrefix(PRNS_PREFIX, PRNS);
    	m.setNsPrefix(R2R_PREFIX, R2R);
		
		DatatypeProperty ts = m.createDatatypeProperty( ADDED_TO_CACHE );
		// allow anything to be added
		//ts.addDomain( foaf.getOntClass( "http://xmlns.com/foaf/0.1/Person" ) );
		ts.addRange( XSD.xlong );			

		//m.read("http://xmlns.com/foaf/spec/", "RDF/XML");
		Ontology ont = m.createOntology( R2R );
		ont.addImport( m.createResource( "http://xmlns.com/foaf/0.1/" ) );
		m.createClass(R2R + "Affiliation");
		m.createClass(R2R + "ColloaboartiveWork");
	
		DatatypeProperty pmid = m.createDatatypeProperty( R2R + "PMID" );
		pmid.addDomain( m.getOntClass( R2R + "ColloaboartiveWork" ) );
		pmid.addRange( XSD.xint );			

		DatatypeProperty pmcid = m.createDatatypeProperty( R2R + "PMCID" );
		pmcid.addDomain( m.getOntClass( R2R + "ColloaboartiveWork" ) );
		pmcid.addRange( XSD.xint );			
		
		DatatypeProperty doi = m.createDatatypeProperty( R2R + "doi" );
		doi.addDomain( m.getOntClass( R2R + "ColloaboartiveWork" ) );
		doi.addRange( XSD.xstring );	
				
		// FOAF extensions
		ObjectProperty aff = m.createObjectProperty( R2R + "affiliation" );
		aff.addDomain( foaf.getOntClass( "http://xmlns.com/foaf/0.1/Person" ) );
		aff.addRange( m.getOntClass( R2R + "Affiliation" ) );		

		ObjectProperty ab = m.createObjectProperty( R2R + "collaboratedOn" );
		ab.addDomain( foaf.getOntClass( "http://xmlns.com/foaf/0.1/Person" ) );
		ab.addRange( m.getOntClass( R2R + "ColloaboartiveWork" ) );		
		
		DatatypeProperty thumbnail = m.createDatatypeProperty( THUMBNAIL );
		thumbnail.addDomain( foaf.getOntClass( "http://xmlns.com/foaf/0.1/Person" ) );
		thumbnail.addRange( XSD.anyURI);	

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
