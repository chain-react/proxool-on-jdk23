/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.examples;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * The simplest example of all. Just gets a Connection.
 *
 * @author chain-react
 */
public class Sample {
	//name of connection pool,  u can use any word
	static String alias = "pool_name";
	
	//below is the MySQL server configuration.
    static String MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";
    static String host = "192.168.0.5";
    static int port = 3306;
    static String db = "database_name";
    static String user = "root";
    static String password = "pwd88889999";
    static String characterEncoding = "utf8";
    
    //load proxool driver
    static {
        try {
            Class.forName("org.logicalcobwebs.proxool.ProxoolDriver");
        } catch (ClassNotFoundException ce) {
            System.err.println("Can not find org.logicalcobwebs.proxool.ProxoolDriver!");
        }
    }
    
    /* get a Connection from pool */
    private static Connection getConnectFromPool() throws SQLException {
    	// url format = proxool_url_wrapper" + real_JDBC_url
        String dbUrl = 
        		//this is proxool_url_wrapper
        		"proxool." + alias + ":" + MYSQL_DRIVER + ":"
        		// below is real_JDBC_url
        		+ "jdbc:mysql://" + host + ":" + port + "/" + db
        		+ "?user=" + user + "&password=" + password
                + "&useUnicode=true"
        		+ "&characterEncoding=" + characterEncoding
                + "&useOldAliasMetadataBehavior=true";
        
        Properties info = new Properties();
        //below will be passed to proxool
        info.setProperty("proxool.maximum-connection-count", "10");
        info.setProperty("proxool.maximum-connection-lifetime", "3600000");
        info.setProperty("proxool.minimum-connection-count", "5");
        info.setProperty("proxool.house-keeping-test-sql", "select CURRENT_DATE");
        //below will be passed to real db driver
        info.setProperty("zeroDateTimeBehavior", "convertToNull");
        info.setProperty("useDynamicCharsetInfo", "false");
        info.setProperty("jdbcCompliantTruncation", "false");
        Connection c = DriverManager.getConnection(dbUrl, info);
        return c;
    }
    
	public static void main(String[] args) throws Exception{
		Connection c = getConnectFromPool();
		Thread.sleep(15*1000);
		// netstat -an | grep 3306
		// At this time, you will see 5 ESTABLISHED TCP.
		
		c.close();
		Thread.sleep(15*1000);
		// netstat -an | grep 3306
		// At this time, you are still seeing the original 5 TCP connections.
		// So, close() just returns the connection to the pool without really closing.
	}
}