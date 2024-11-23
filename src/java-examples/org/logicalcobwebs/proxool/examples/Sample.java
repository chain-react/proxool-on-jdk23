package org.logicalcobwebs.proxool.examples;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * The MySQL example.
 * @author chain-react
 */
public class Sample {
    static String alias = "pool_name";//name of connection pool.
    static boolean inited = false;//is pool inited.

    /* init a connection pool */
    private static void initConnectionPool() throws Exception {
        // load proxool driver class
        Class.forName("org.logicalcobwebs.proxool.ProxoolDriver");

        // below is the MySQL server configuration.
        String MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";
        String host = "192.168.0.5";
        int port = 3306;
        String db = "database_name";
        String user = "root";
        String password = "pwd88889999";
        String characterEncoding = "utf8";

        // proxool url format = proxool_wrapper + real_JDBC_url
        String dbUrl = 
            // below is proxool_wrapper
            "proxool." + alias + ":" + MYSQL_DRIVER + ":"
            // below is real_JDBC_url
            + "jdbc:mysql://" + host + ":" + port + "/" + db
            + "?user=" + user + "&password=" + password
            + "&useUnicode=true"
            + "&characterEncoding=" + characterEncoding
            + "&useOldAliasMetadataBehavior=true";
        
        Properties info = new Properties();
        // below will be passed to proxool pool( name starts with "proxool." )
        info.setProperty("proxool.maximum-connection-count", "10");
        info.setProperty("proxool.maximum-connection-lifetime", "3600000");
        info.setProperty("proxool.minimum-connection-count", "5");
        info.setProperty("proxool.house-keeping-test-sql", "select CURRENT_DATE");
        // below will be passed to real db driver
        info.setProperty("zeroDateTimeBehavior", "convertToNull");
        info.setProperty("useDynamicCharsetInfo", "false");
        info.setProperty("jdbcCompliantTruncation", "false");

        // create pool
        Connection c = DriverManager.getConnection(dbUrl, info);
        c.close();

        inited = true;
    }
    
    /* get a connection with thread-safe.*/
    public static Connection getConnectFromPool() throws Exception{
        if( ! inited ){
            synchronized( Sample.class ){
                if( ! inited ){
                    initConnectionPool();
                }
            }
        }
        return DriverManager.getConnection("proxool." + alias, null);
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