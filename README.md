## proxool-on-jdk23

A Java SQL Driver that provides a connection pool wrapper

## Features

- It can run on any version between JDK 1.4 and JDK 23.0
- It does not depend on any third-party JAR libraries
- The package name, class name, and interface name are exactly the same as the old `proxool` library; so, all you need to do is replace the JAR file

## Usage

Below code is a simple example of connecting to a MySQL database.

You can observe MySQL connection pool by running the command "netstat -an | grep 3306".

```java
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
    
    //get a Connection from pool
    private static Connection getConnectFromPool() throws SQLException {
    	// url format = proxool_url_wrapper + real_JDBC_url
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
        // So, method close() just return the connection to the pool without really closing.
    }
}
```

## Note

The original author of the Proxool is Bill Horsman, see the [Author Site](https://github.com/proxool/proxool)
