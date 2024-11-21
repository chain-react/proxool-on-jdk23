/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * A central place to build proxy objects ({@link ProxyConnection connections}
 * and {@link ProxyStatement statements}).
 * 
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @version $Revision: 1.25 $, $Date: 2003/12/12 19:29:47 $
 * @since Proxool 0.5
 */
class ProxyFactory {
    protected static ProxyConnection buildProxyConnection(long id, ConnectionPool connectionPool, int status) throws SQLException {
        Connection realConnection = null;
        final String url = connectionPool.getDefinition().getUrl();

        Properties info = connectionPool.getDefinition().getDelegateProperties();
        realConnection = DriverManager.getConnection(url, info);

        Object delegate = Proxy.newProxyInstance(
                realConnection.getClass().getClassLoader(),
                realConnection.getClass().getInterfaces(),
                new ProxyConnection(realConnection, id, url, connectionPool, status));
        
        return (ProxyConnection) Proxy.getInvocationHandler(delegate);
    }

    /**
     * Get a Connection from the ProxyConnection
     * 
     * @param proxyConnection where to find the connection
     * @return 
     */
    protected static Connection getConnection(ProxyConnectionIF proxyConnection) {
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                (InvocationHandler) proxyConnection);
    }

    /**
     * Gets the real Statement that we got from the delegate driver
     * 
     * @param statement proxy statement
     * @return delegate statement
     */
    protected static Statement getDelegateStatement(Statement statement) {
        Statement ds = statement;
        ProxyStatement ps = (ProxyStatement) Proxy.getInvocationHandler(statement);
        ds = ps.getDelegateStatement();
        return ds;
    }

    /**
     * Gets the real Connection that we got from the delegate driver
     * 
     * @param connection proxy connection
     * @return deletgate connection
     */
    public static Connection getDelegateConnection(Connection connection) {
        Connection c = connection;
        ProxyConnection pc = (ProxyConnection) Proxy.getInvocationHandler(connection);
        c = pc.getConnection();
        return c;
    }

    protected static Statement createProxyStatement(Statement delegate, ConnectionPool connectionPool, ProxyConnectionIF proxyConnection, String sqlStatement) {
        // We can't use Class#getInterfaces since that doesn't take
        // into account superclass interfaces. We could, laboriously,
        // work our way up the hierarchy but it doesn't seem worth while -
        // we only actually expect three options:
        Class[] interfaces = new Class[1];
        if (delegate instanceof CallableStatement) {
            interfaces[0] = CallableStatement.class;
        } else if (delegate instanceof PreparedStatement) {
            interfaces[0] = PreparedStatement.class;
        } else {
            interfaces[0] = Statement.class;
        }
/*
        if (LOG.isDebugEnabled()) {
            LOG.debug(delegate.getClass().getName() + " is being proxied using the " + interfaces[0]);
        }
*/
        return (Statement) Proxy.newProxyInstance(delegate.getClass().getClassLoader(), interfaces, new ProxyStatement(delegate, connectionPool, proxyConnection, sqlStatement));
    }

    /**
     * Create a new DatabaseMetaData from a connection
     * 
     * @param connection the proxy connection we are using
     * @return databaseMetaData
     * @throws SQLException if the delegfate connection couldn't get the metaData
     */
    protected static DatabaseMetaData getDatabaseMetaData(Connection connection, ProxyConnectionIF proxyConnection) throws SQLException {
        return (DatabaseMetaData) Proxy.newProxyInstance(
                DatabaseMetaData.class.getClassLoader(),
                new Class[]{DatabaseMetaData.class},
                new ProxyDatabaseMetaData(connection, proxyConnection)
        );
    }
}

/*
 Revision history:
 $Log: ProxyFactory.java,v $
 Revision 1.25  2003/12/12 19:29:47  billhorsman
 Now uses Cglib 2.0

 Revision 1.24  2003/09/30 18:39:08  billhorsman
 New test-before-use, test-after-use and fatal-sql-exception-wrapper-class properties.

 Revision 1.23  2003/09/10 22:21:04  chr32
 Removing > jdk 1.2 dependencies.

 Revision 1.22  2003/09/07 22:11:31  billhorsman
 Remove very persistent debug message

 Revision 1.21  2003/08/27 18:03:20  billhorsman
 added new getDelegateConnection() method

 Revision 1.20  2003/03/11 14:51:54  billhorsman
 more concurrency fixes relating to snapshots

 Revision 1.19  2003/03/10 23:43:13  billhorsman
 reapplied checkstyle that i'd inadvertently let
 IntelliJ change...

 Revision 1.18  2003/03/10 15:26:49  billhorsman
 refactoringn of concurrency stuff (and some import
 optimisation)

 Revision 1.17  2003/03/05 18:42:33  billhorsman
 big refactor of prototyping and house keeping to
 drastically reduce the number of threads when using
 many pools

 Revision 1.16  2003/03/03 11:11:58  billhorsman
 fixed licence

 Revision 1.15  2003/02/19 15:14:32  billhorsman
 fixed copyright (copy and paste error,
 not copyright change)

 Revision 1.14  2003/02/12 12:28:27  billhorsman
 added url, proxyHashcode and delegateHashcode to
 ConnectionInfoIF

 Revision 1.13  2003/02/06 17:41:04  billhorsman
 now uses imported logging

 Revision 1.12  2003/01/31 14:33:18  billhorsman
 fix for DatabaseMetaData

 Revision 1.11  2003/01/27 18:26:39  billhorsman
 refactoring of ProxyConnection and ProxyStatement to
 make it easier to write JDK 1.2 patch

 Revision 1.10  2002/12/16 11:15:19  billhorsman
 fixed getDelegateStatement

 Revision 1.9  2002/12/16 10:57:47  billhorsman
 add getDelegateStatement to allow access to the
 delegate JDBC driver's Statement

 Revision 1.8  2002/12/12 10:48:25  billhorsman
 checkstyle

 Revision 1.7  2002/12/08 22:17:35  billhorsman
 debug for proxying statement interfaces

 Revision 1.6  2002/12/06 15:57:08  billhorsman
 fix for proxied statement where Statement interface is not directly
 implemented.

 Revision 1.5  2002/12/03 12:24:00  billhorsman
 fixed fatal sql exception

 Revision 1.4  2002/11/09 15:56:52  billhorsman
 fix doc

 Revision 1.3  2002/11/02 14:22:15  billhorsman
 Documentation

 Revision 1.2  2002/10/30 21:25:08  billhorsman
 move createStatement into ProxyFactory

 Revision 1.1  2002/10/30 21:19:16  billhorsman
 make use of ProxyFactory

*/
