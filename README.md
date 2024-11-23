## Introduction | 简介

A Java SQL Driver that provides a connection pool wrapper

一个JDBC数据库连接池的封装

## Features | 功能特性

- It can run on any version between JDK 1.4 and JDK 23.0
- It does not depend on any third-party JAR libraries
- The package name, class name, and interface name are exactly the same as the old `proxool` library; so, all you need to do is replace the JAR file
- 可以运行在 JDK 1.4 到 JDK 23.0 的所有Java版本上
- 不依赖于任何第三方JAR库，仅仅依赖JDK本身
- 包名，类名，接口名和老的`proxool`库一模一样，所以，你要做的仅仅是替换老的JAR文件

## Usage | 用法

Below code is a simple example of connecting to a MySQL database.

You can observe MySQL connection pool by running the command "netstat -an | grep 3306".

下面是实现一个MySQL数据库连接池的范例

你可以通过"netstat -an | grep 3306"命令，观察MySQL的连接情况

```java
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
```

## Note | 备注

The original author of the Proxool is Bill Horsman, see the [Author Site](https://github.com/proxool/proxool)

Proxool的原作者是Bill Horsman，点击此处可以看[原作者网站](https://github.com/proxool/proxool)
