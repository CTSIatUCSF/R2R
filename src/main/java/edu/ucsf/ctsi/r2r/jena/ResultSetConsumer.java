package edu.ucsf.ctsi.r2r.jena;

import org.apache.jena.query.ResultSet;

public interface ResultSetConsumer {

	void useResultSet(ResultSet rs) throws Exception;

}
