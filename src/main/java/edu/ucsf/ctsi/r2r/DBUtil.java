package edu.ucsf.ctsi.r2r;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class DBUtil {

	private String dbUrl = "jdbc:sqlserver://stage-sql-ctsi.ucsf.edu;instanceName=default;portNumber=1433;databaseName=crosslink";
	private String dbUser = "crosslink";
	private String dbPassword = "crosslink";

	private static final Logger LOG = Logger.getLogger(DBUtil.class.getName());
	
	@Inject
	public DBUtil(@Named("dbUrl") String dbUrl, @Named("dbUser") String dbUser, @Named("dbPassword") String dbPassword) throws ClassNotFoundException {
		Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		this.dbUrl = dbUrl;
		this.dbUser = dbUser;
		this.dbPassword = dbPassword;
	}
	
    public Connection getConnection() {
        try {
            Connection conn = DriverManager.getConnection(dbUrl, dbUser,
                    dbPassword);
            return conn;
        } catch (SQLException e) {
			LOG.log(Level.WARNING, "Can not connect to " + dbUrl, e);
            return null;
        }
    }

    public void unloadDrivers() {
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                LOG.log(Level.INFO, String.format("deregistering jdbc driver: %s", driver));
            } catch (SQLException e) {
                LOG.log(Level.SEVERE, String.format("Error deregistering driver %s", driver), e);
            }

        }
    }	
}
