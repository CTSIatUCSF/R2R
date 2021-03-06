package edu.ucsf.ctsi.r2r;

import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.XSD;

public class R2ROntology implements R2RConstants {
	
	private static byte[] foafFile = null;
	private static byte[] geoFile = null;
			
	static {
		try {
			foafFile = IOUtils.toByteArray(R2ROntology.class.getResourceAsStream("/foaf.rdf"));
			geoFile = IOUtils.toByteArray(R2ROntology.class.getResourceAsStream("/geo.rdf"));
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
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
		model.setNsPrefix(GEO_PREFIX, GEO);
		model.setNsPrefix(VIVO_PREFIX, VIVO);
		model.setNsPrefix(PRNS_PREFIX, PRNS);
		model.setNsPrefix(R2R_PREFIX, R2R);
	}
	
	public static OntModel createR2ROntModel() {
		OntModel ontModel = ModelFactory.createOntologyModel();
		addNS(ontModel);
		//ontModel.read("http://xmlns.com/foaf/spec/", "RDF/XML");
		//ontModel.read(GEO, "RDF/XML");
		ontModel.read(new ByteArrayInputStream(foafFile), null);
		ontModel.read(new ByteArrayInputStream(geoFile), null);

		OntClass crawler = ontModel.createClass(R2R_PROCESSOR);
		OntClass processorRun = ontModel.createClass(R2R_PROCESSOR_RUN);
		OntClass affiliation = ontModel.createClass(R2R_AFFILIATION);
		OntClass person = ontModel.createClass( FOAF_PERSON );
				
		// anything can have this
		DatatypeProperty ts = ontModel.createDatatypeProperty( R2R_ADDED_TO_CACHE );
		ts.addRange( XSD.dateTime );
		ontModel.createMaxCardinalityRestriction(null, ts, 1);

		// Crawler properties
		DatatypeProperty cst = ontModel.createDatatypeProperty( R2R_PROCESSOR_START_DT );
		cst.addDomain( crawler );
		cst.addRange( XSD.dateTime );			
		ontModel.createMaxCardinalityRestriction(null, cst, 1);
				
		DatatypeProperty cet = ontModel.createDatatypeProperty( R2R_PROCESSOR_END_DT );
		cet.addDomain( crawler );
		cet.addRange( XSD.dateTime );			
		ontModel.createMaxCardinalityRestriction(null, cet, 1);

		// crawler run
		DatatypeProperty proc = ontModel.createDatatypeProperty( R2R_PROCESSED );
		proc.addDomain( person );
		proc.addRange( processorRun );		

		DatatypeProperty cro = ontModel.createDatatypeProperty( R2R_PROCESSED_ON );
		cro.addDomain( processorRun );
		cro.addRange( XSD.dateTime );			
		ontModel.createMaxCardinalityRestriction(null, cro, 1);
		
		ObjectProperty hf = ontModel.createObjectProperty( R2R_PROCESSED_BY );
		hf.addDomain( processorRun );
		hf.addRange( crawler );		
		ontModel.createMaxCardinalityRestriction(null, hf, 1);

		// affiliation
		affiliation.addSuperClass(ontModel.createClass(GEO_SPATIALTHING));
		ObjectProperty icon = ontModel.createObjectProperty(R2R_HAS_ICON);
		icon.addDomain( affiliation );
		icon.addRange(ontModel.createResource(FOAF_IMAGE));
		ontModel.createMaxCardinalityRestriction(null, icon, 1);

		// Person properties
		ObjectProperty aff = ontModel.createObjectProperty( R2R_HAS_AFFILIATION );
		aff.addDomain( person );
		aff.addRange( affiliation );		
		ontModel.createMaxCardinalityRestriction(null, aff, 1);

		// derived cnt's
		DatatypeProperty ecc = ontModel.createDatatypeProperty( R2R_EXTERNAL_COAUTHOR_CNT );
		ecc.addDomain( person );
		ecc.addRange( XSD.integer );			
		ontModel.createMaxCardinalityRestriction(null, ecc, 1);

		DatatypeProperty spc = ontModel.createDatatypeProperty( R2R_SHARED_PUB_CNT );
		spc.addDomain( person );
		spc.addRange( XSD.integer );			
		ontModel.createMaxCardinalityRestriction(null, spc, 1);

		return ontModel;
	}
	
	public static void printModel(String filename) throws IOException {
		OntModel m = createR2ROntModel();
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
//			OntModel m = createR2ROntModel();
//			OntProperty p = (OntProperty) m.getProperty( R2R_HARVESTED_FROM );
//			Iterator<Restriction> i = p.listReferringRestrictions();
//			while (i.hasNext()) {
//			    Restriction r = i.next();
//			    if (r.isMaxCardinalityRestriction())
//			    	System.out.println(r.asMaxCardinalityRestriction().getMaxCardinality());
//			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}	
}
