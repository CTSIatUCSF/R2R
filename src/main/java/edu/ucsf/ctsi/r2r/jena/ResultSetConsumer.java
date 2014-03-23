package edu.ucsf.ctsi.r2r.jena;

import com.hp.hpl.jena.query.ResultSet;

public interface ResultSetConsumer {

	void useResultSet(ResultSet rs);

}
