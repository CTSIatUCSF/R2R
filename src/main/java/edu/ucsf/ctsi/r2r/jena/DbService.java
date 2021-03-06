package edu.ucsf.ctsi.r2r.jena;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.ucsf.ctsi.r2r.DBUtil;
import edu.ucsf.ctsi.r2r.R2ROntology;

public class DbService implements ModelService, RDFXMLService, ResourceService, DecryptionService {

	private static final Logger LOG = Logger.getLogger(DbService.class.getName());
	
	private String orngUser;
	private String systemBase;
	private DBUtil dbUtil;
	private boolean showDetails = true;
	private boolean expand = false;
	private String sessionId;
	
	@Inject
	public DbService(@Named("orng.systemDomain") String systemDomain,  @Named("orng.user") String orngUser, DBUtil dbUtil) throws SQLException {
		this.systemBase = systemDomain + "/profile/";
		this.orngUser = orngUser;
		// set up the cache
    	this.dbUtil = dbUtil;    	
		Connection conn = dbUtil.getConnection();        
		try {
			sessionId = getSessionId(conn);
		}
		finally {
			conn.close();
		}
	}
	
	// inject someday
	public void setProfilesOptions(boolean showDetails, boolean expand) {
		this.showDetails = showDetails;
		this.expand = expand;
	}
	
	public Model getModel(String uri) throws Exception {
		Integer nodeId = getNodeId(uri);
		if (nodeId == null) {
			return null;
		}
		Model model = R2ROntology.createDefaultModel();
		Connection conn = dbUtil.getConnection();        
		try {
			loadIntoModel(model, sessionId, conn, nodeId, showDetails, expand);
		}
		finally {
			conn.close();
		}
		return model;
	}

	public byte[] getRDFXML(String uri) throws Exception {
		Integer nodeId = getNodeId(uri);
		if (nodeId == null) {
			return null;
		}
		Connection conn = dbUtil.getConnection();        
		try {
			return getRDFXML(sessionId, conn, nodeId, showDetails, expand);
		}
		finally {
			conn.close();
		}
	}
	
	public String decrypt(String encrypted) throws Exception {
		Connection conn = dbUtil.getConnection();
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT [Utility.Application].[fnDecryptBase64RC4] ( '" + encrypted + "',   (Select [value] from [Framework.].parameter with(nolock) where ParameterID = 'RC4EncryptionKey'))");
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getString(1);
			}
			
		}
		catch (SQLException se) {
	        LOG.log(Level.SEVERE, "Error decrpting " + encrypted, se);
		}
		finally {
			try { conn.close(); } catch (SQLException se) {
		        LOG.log(Level.SEVERE, "Error closing connection", se);
			}
		}
		return null;
	}

	private Integer getNodeId(String uri) {
		if (uri.toLowerCase().startsWith(systemBase)) {
			return Integer.parseInt(uri.split(systemBase)[1]);
		}
		return null;
	}
	
	private String getSessionId(Connection conn) throws SQLException {
		// log in, maybe check if username is null and do anonymous TDB in that case
		if (orngUser != null) {
	        CallableStatement cs = conn.prepareCall("{ call [User.Account].[AuthenticateExternal](?, ?)}");
	        cs.setString(1, orngUser);
	        cs.registerOutParameter(2, java.sql.Types.INTEGER);
	        cs.executeUpdate();
	        int userId = cs.getInt(2);
	        cs.close();
	        
	        cs = conn.prepareCall("{ call [User.Session].[CreateSession](?, ?, ?, ?)}");
	        cs.setString(1, "127.0.0.1"); // IP
	        cs.setString(2, "localhost"); // hostname
	        cs.setString(3, "ORNG"); // useragent
	        cs.setInt(4, userId); // user id 
	        ResultSet rs = cs.executeQuery();
	        if (rs.next()) {
	        	return rs.getString(1);
	        }
		}
		return null;
	}
	
	private ResultSet getResultSet(String sessionId, Connection conn, int nodeId, boolean showDetails, boolean expand) throws SQLException {
        CallableStatement cs = conn.prepareCall("{ call [RDF.].[GetDataRDF](?, ?, ?, ?, ? ,? ,? ,?)}");
        cs.setInt(1, nodeId);
        cs.setNull(2, java.sql.Types.BIGINT);
        cs.setNull(3, java.sql.Types.BIGINT);
        cs.setNull(4, java.sql.Types.BIGINT);
        cs.setNull(5, java.sql.Types.BIGINT);
        cs.setBoolean(6, showDetails);
        cs.setBoolean(7, expand);
        cs.setString(8, sessionId);

       return cs.executeQuery();		
	}
	
	private byte[] getRDFXML(String sessionId, Connection conn, int nodeId, boolean showDetails, boolean expand) throws SQLException {
		byte[] retval = null;
		ResultSet rs = getResultSet(sessionId, conn, nodeId, showDetails, expand);
        if (rs.next()) {
        	retval = rs.getBytes(1);
        }
    	LOG.info("Just read " + nodeId + " from the database");	        
    	return retval;
	}
	
	private void loadIntoModel(Model model, String sessionId, Connection conn, int nodeId, boolean showDetails, boolean expand) throws SQLException {
		ResultSet rs = getResultSet(sessionId, conn, nodeId, showDetails, expand);
        if (rs.next()) {
        	SQLXML sx = rs.getSQLXML(1);
            model.read(sx.getCharacterStream(), null);
        }
    	LOG.info("Just read " + nodeId + " from the database");	        	
	}

	public Resource getResource(String uri) throws Exception {
		return getModel(uri).createResource(uri);
	}
	
}
