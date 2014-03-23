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
			
	public static OntModel getR2ROntModel() {
		OntModel m = ModelFactory.createOntologyModel();
		//m.read("http://xmlns.com/foaf/spec/", "RDF/XML");
		Ontology ont = m.createOntology( SOURCE );
		ont.addImport( m.createResource( "http://xmlns.com/foaf/0.1/" ) );
		m.createClass(NS + "ColloaboartiveWork");
	
		DatatypeProperty pmid = m.createDatatypeProperty( NS + "PMID" );
		pmid.addDomain( m.getOntClass( NS + "ColloaboartiveWork" ) );
		pmid.addRange( XSD.xint );			

		DatatypeProperty pmcid = m.createDatatypeProperty( NS + "PMCID" );
		pmcid.addDomain( m.getOntClass( NS + "ColloaboartiveWork" ) );
		pmcid.addRange( XSD.xint );			
		
		DatatypeProperty doi = m.createDatatypeProperty( NS + "doi" );
		doi.addDomain( m.getOntClass( NS + "ColloaboartiveWork" ) );
		doi.addRange( XSD.xstring );	
				
		// FOAF extensions
		OntModel foaf = ModelFactory.createOntologyModel();
		foaf.read("http://xmlns.com/foaf/spec/", "RDF/XML");
		
		ObjectProperty ab = m.createObjectProperty( NS + "collaboratedOn" );
		ab.addDomain( foaf.getOntClass( "http://xmlns.com/foaf/0.1/Person" ) );
		ab.addRange( m.getOntClass( NS + "ColloaboartiveWork" ) );		
		
		DatatypeProperty ts = m.createDatatypeProperty( ADDED_TO_CACHE );
		// allow anything to be added
		//ts.addDomain( foaf.getOntClass( "http://xmlns.com/foaf/0.1/Person" ) );
		ts.addRange( XSD.xlong );			

		return m;
	}
	
	public static void printModel(String filename) throws IOException {
		OntModel m = getR2ROntModel();
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
			if ("print".equalsIgnoreCase(args[0])) {
				printModel(args.length > 1 ? args[1] : "R2R.owl");				
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}	
}
