/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Delegates to a normal Coonection for everything but the close()
 * method (when it puts itself back into the pool instead).
 * @version $Revision: 1.29 $, $Date: 2003/12/12 19:29:47 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
class ProxyConnection extends AbstractProxyConnection implements InvocationHandler {

    private static final Log LOG = LogFactory.getLog(ProxyConnection.class);

    private static final String CLOSE_METHOD = "close";

    private static final String IS_CLOSED_METHOD = "isClosed";

    private static final String EQUALS_METHOD = "equals";

    private static final String GET_META_DATA_METHOD = "getMetaData";

    private static final String FINALIZE_METHOD = "finalize";

    public ProxyConnection(Connection connection, long id, String delegateUrl, ConnectionPool connectionPool, int status) throws SQLException {
        super(connection, id, delegateUrl, connectionPool, status);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = null;
        int argCount = args != null ? args.length : 0;
        try {
            if (method.getName().equals(CLOSE_METHOD)) {
                close();
            } else if (method.getName().equals(EQUALS_METHOD) && argCount == 1) {
                result = new Boolean(equals(args[0]));
            } else if (method.getName().equals(IS_CLOSED_METHOD) && argCount == 0) {
                result = new Boolean(isClosed());
            } else if (method.getName().equals(GET_META_DATA_METHOD) && argCount == 0) {
                result = getMetaData();
            } else if (method.getName().equals(FINALIZE_METHOD)) {
                super.finalize();
            } else {
                if (method.getName().startsWith(ConnectionResetter.MUTATOR_PREFIX)) {
                    setNeedToReset(true);
                }
                    result = method.invoke(getConnection(), args);
            }

            // If we have just made some sort of Statement then we should rather return
            // a proxy instead.
            if (result instanceof Statement) {
                // Work out whether we were passed the sql statement during the
                // call to get the statement object. Sometimes you do, sometimes
                // you don't:
                // connection.prepareCall(sql);
                // connection.createProxyStatement();
                String sqlStatement = null;
                if (argCount > 0 && args[0] instanceof String) {
                    sqlStatement = (String) args[0];
                }

                // We keep a track of all open statements
                addOpenStatement((Statement) result);

                result = ProxyFactory.createProxyStatement((Statement) result, getConnectionPool(), this, sqlStatement);

            }

        } catch (InvocationTargetException e) {
            // We might get a fatal exception here. Let's test for it.
            if (FatalSqlExceptionHelper.testException(getConnectionPool().getDefinition(), e.getTargetException())) {
                FatalSqlExceptionHelper.throwFatalSQLException(getConnectionPool().getDefinition().getFatalSqlExceptionWrapper(), e.getTargetException());
            }
            throw e.getTargetException();
        } catch (Exception e) {
            LOG.error("Unexpected invocation exception", e);
            if (FatalSqlExceptionHelper.testException(getConnectionPool().getDefinition(), e)) {
                FatalSqlExceptionHelper.throwFatalSQLException(getConnectionPool().getDefinition().getFatalSqlExceptionWrapper(), e);
            }
            throw new RuntimeException("Unexpected invocation exception: "
                    + e.getMessage());
        }

        return result;
    }

}

/*
 Revision history:
 $Log: ProxyConnection.java,v $
 Revision 1.29  2003/12/12 19:29:47  billhorsman
 Now uses Cglib 2.0

 Revision 1.28  2003/09/30 18:39:08  billhorsman
 New test-before-use, test-after-use and fatal-sql-exception-wrapper-class properties.

 Revision 1.27  2003/09/10 22:21:04  chr32
 Removing > jdk 1.2 dependencies.

 Revision 1.26  2003/03/11 14:51:54  billhorsman
 more concurrency fixes relating to snapshots

 Revision 1.25  2003/03/03 11:11:58  billhorsman
 fixed licence

 Revision 1.24  2003/02/12 12:28:27  billhorsman
 added url, proxyHashcode and delegateHashcode to
 ConnectionInfoIF

 Revision 1.23  2003/02/06 17:41:04  billhorsman
 now uses imported logging

 Revision 1.22  2003/01/31 14:33:16  billhorsman
 fix for DatabaseMetaData

 Revision 1.21  2003/01/27 18:26:38  billhorsman
 refactoring of ProxyConnection and ProxyStatement to
 make it easier to write JDK 1.2 patch

 Revision 1.20  2002/12/19 00:08:36  billhorsman
 automatic closure of statements when a connection is closed

 Revision 1.19  2002/12/17 17:15:39  billhorsman
 Better synchronization of status stuff

 Revision 1.18  2002/12/03 12:24:00  billhorsman
 fixed fatal sql exception

 Revision 1.17  2002/11/12 20:24:12  billhorsman
 checkstyle

 Revision 1.16  2002/11/12 20:18:23  billhorsman
 Made connection resetter a bit more friendly. Now, if it encounters any problems
 during reset then that connection is thrown away. This is going to cause you
 problems if you always close connections in an unstable state (e.g. with transactions
 open. But then again, it's better to know about that as soon as possible, right?

 Revision 1.15  2002/11/07 18:56:22  billhorsman
 fixed NullPointerException introduced yesterday on isClose() method

 Revision 1.14  2002/11/07 12:38:04  billhorsman
 performance improvement - only reset when it might be necessary

 Revision 1.13  2002/11/06 20:26:49  billhorsman
 improved doc, added connection resetting, and made
 isClosed() work correctly

 Revision 1.12  2002/11/02 13:57:33  billhorsman
 checkstyle

 Revision 1.11  2002/10/30 21:25:09  billhorsman
 move createStatement into ProxyFactory

 Revision 1.10  2002/10/30 21:19:17  billhorsman
 make use of ProxyFactory

 Revision 1.9  2002/10/28 19:51:34  billhorsman
 Fixed NullPointerException when calling connection.createProxyStatement()

 Revision 1.8  2002/10/28 19:28:25  billhorsman
 checkstyle

 Revision 1.7  2002/10/28 08:20:23  billhorsman
 draft sql dump stuff

 Revision 1.6  2002/10/25 15:59:32  billhorsman
 made non-public where possible

 Revision 1.5  2002/10/24 18:15:09  billhorsman
 removed unnecessary debug

 Revision 1.4  2002/10/17 15:29:18  billhorsman
 fixes so that equals() works

 Revision 1.3  2002/09/19 10:33:57  billhorsman
 added ProxyConnection#toString

 Revision 1.2  2002/09/18 13:48:56  billhorsman
 checkstyle and doc

 Revision 1.1.1.1  2002/09/13 08:13:30  billhorsman
 new

 Revision 1.10  2002/08/24 19:57:15  billhorsman
 checkstyle changes

 Revision 1.9  2002/08/24 19:42:26  billhorsman
 new proxy stuff to work with JDK 1.4

 Revision 1.6  2002/07/02 11:19:08  billhorsman
 layout code and imports

 Revision 1.5  2002/06/28 11:19:47  billhorsman
 improved doc

*/
