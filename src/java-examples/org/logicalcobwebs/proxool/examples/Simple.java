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
public class Simple {
	static String alias = "alias";//alias of connection pool
	
	//Below is the MySQL server configuration.
    static final String MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";
    static String host = "mysql.xxx.com";
    static int port = 3306;
    static String db = "database_name";
    static String user = "root";
    static String password = "pwd88889999";
    static String characterEncoding = "utf8";
    
    private static Connection getConnectionByProxool() throws SQLException {
    	
        try {
            Class.forName("org.logicalcobwebs.proxool.ProxoolDriver");
        } catch (ClassNotFoundException ce) {
            throw new SQLException("Can not find org.logicalcobwebs.proxool.ProxoolDriver!");
        }
    	
        String dburl = "proxool." + alias + ":" + MYSQL_DRIVER + ":jdbc:mysql://" + host + ":" + port + "/" + db + "?user=" + user + "&password=" + password
                + "&useUnicode=true&characterEncoding=" + characterEncoding + "&useOldAliasMetadataBehavior=true";
        Properties info = new Properties();
        info.setProperty("proxool.maximum-connection-count", "3");
        info.setProperty("proxool.maximum-connection-lifetime", "3600000");
        info.setProperty("proxool.minimum-connection-count", "3");
        info.setProperty("proxool.house-keeping-test-sql", "select CURRENT_DATE");
        info.setProperty("zeroDateTimeBehavior", "convertToNull");
        info.setProperty("useDynamicCharsetInfo", "false");
        info.setProperty("jdbcCompliantTruncation", "false");
        Connection c = DriverManager.getConnection(dburl, info);
        return c;
    }
    
	public static void main(String[] args) throws Exception{
		Connection c = getConnectionByProxool();
		System.out.println(c.getClass());
		ResultSet rs = c.createStatement().executeQuery("select * from t_user limit 3");
		System.out.println(rs.getClass());
		while(rs.next()) {
			System.out.println(rs.getLong("uid"));
		}
		c.close();
		
		// netstat -an | grep 3306
		// u can see "minimum-connection-count" ESTABLISHED tcp
		Thread.sleep(15*1000);

		c=getConnectionByProxool();
		rs = c.createStatement().executeQuery("select * from t_user limit 3");
		while(rs.next()) {
			System.out.println(rs.getString("uname"));
		}
		c.close();
		
		// netstat -an | grep 3306
		// u can see "minimum-connection-count" ESTABLISHED tcp
		Thread.sleep(15*1000);
	}
}