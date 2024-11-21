/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Statement;

/**
 * Delegates to Statement for all calls. But also, for all execute methods, it
 * checks the SQLException and compares it to the fatalSqlException list in the
 * ConnectionPoolDefinition. If it detects a fatal exception it will destroy the
 * Connection so that it isn't used again.
 * @version $Revision: 1.25 $, $Date: 2003/12/12 19:29:47 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
class ProxyStatement extends AbstractProxyStatement implements java.lang.reflect.InvocationHandler{

    private static final Log LOG = LogFactory.getLog(ProxyStatement.class);

    private static final String EXECUTE_FRAGMENT = "execute";

    private static final String EXECUTE_BATCH_METHOD = "executeBatch";

    private static final String ADD_BATCH_METHOD = "addBatch";

    private static final String EQUALS_METHOD = "equals";

    private static final String CLOSE_METHOD = "close";

    private static final String SET_NULL_METHOD = "setNull";

    private static final String SET_PREFIX = "set";

    public ProxyStatement(Statement statement, ConnectionPool connectionPool, ProxyConnectionIF proxyConnection, String sqlStatement) {
        super(statement, connectionPool, proxyConnection, sqlStatement);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = null;
        long startTime = System.currentTimeMillis();
        final int argCount = args != null ? args.length : 0;

        // We need to remember an exceptions that get thrown so that we can optionally
        // pass them to the onExecute() call below
        Exception exception = null;
        try {
            if (method.getName().equals(EQUALS_METHOD) && argCount == 1) {
                result = new Boolean(equals(args[0]));
            } else if (method.getName().equals(CLOSE_METHOD) && argCount == 0) {
                close();
            } else {
                result = method.invoke(getStatement(), args);
            }

            // We only dump sql calls if we are in verbose mode and debug is enabled
            if (isTrace()) {
                try {

                    // What sort of method is it
                    if (method.getName().equals(SET_NULL_METHOD) && argCount > 0 && args[0] instanceof Integer) {
                        int index = ((Integer) args[0]).intValue();
                        putParameter(index, null);
                    } else if (method.getName().startsWith(SET_PREFIX) && argCount > 1 && args[0] instanceof Integer) {
                        int index = ((Integer) args[0]).intValue();
                        putParameter(index, args[1]);
                    }

                } catch (Exception e) {
                    // We don't want an error during dump screwing up the transaction
                    LOG.error("Ignoring error during dump", e);
                }
            }
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof Exception) {
                exception = (Exception) e.getTargetException();
            } else {
                exception = e;
            }
            if (testException(e.getTargetException())) {
                // This is really a fatal one
                FatalSqlExceptionHelper.throwFatalSQLException(getConnectionPool().getDefinition().getFatalSqlExceptionWrapper(), e.getTargetException());
            }
            throw e.getTargetException();
        } catch (Exception e) {
            exception = e;
            if (testException(e)) {
                // This is really a fatal one
                FatalSqlExceptionHelper.throwFatalSQLException(getConnectionPool().getDefinition().getFatalSqlExceptionWrapper(), e);
            }
            throw e;
        } finally {

            if (method.getName().equals(ADD_BATCH_METHOD)) {
                // If we have just added a batch call then we need to update the sql log
                if (argCount > 0 && args[0] instanceof String) {
                    setSqlStatementIfNull((String) args[0]);
                }
                appendToSqlLog();
            } else if (method.getName().equals(EXECUTE_BATCH_METHOD)) {
                // executing a batch should do a trace
                trace(startTime, exception);
            } else if (method.getName().startsWith(EXECUTE_FRAGMENT)) {
                // executing should update the log and do a trace
                if (argCount > 0 && args[0] instanceof String) {
                    setSqlStatementIfNull((String) args[0]);
                }
                appendToSqlLog();
                trace(startTime, exception);
            }

        }

        return result;

    }

}

/*
 Revision history:
 $Log: ProxyStatement.java,v $
 Revision 1.25  2003/12/12 19:29:47  billhorsman
 Now uses Cglib 2.0

 Revision 1.24  2003/10/19 09:50:33  billhorsman
 Drill down into InvocationTargetException during execution debug.

 Revision 1.23  2003/10/18 20:44:48  billhorsman
 Better SQL logging (embed parameter values within SQL call) and works properly with batched statements now.

 Revision 1.22  2003/09/30 18:39:08  billhorsman
 New test-before-use, test-after-use and fatal-sql-exception-wrapper-class properties.

 Revision 1.21  2003/09/29 17:48:49  billhorsman
 New fatal-sql-exception-wrapper-class allows you to define what exception is used as a wrapper. This means that you
 can make it a RuntimeException if you need to.

 Revision 1.20  2003/09/10 22:21:04  chr32
 Removing > jdk 1.2 dependencies.

 Revision 1.19  2003/09/05 17:01:00  billhorsman
 Trap and throw FatalSQLExceptions.

 Revision 1.18  2003/03/03 11:11:58  billhorsman
 fixed licence

 Revision 1.17  2003/02/13 17:06:42  billhorsman
 allow for sqlStatement in execute() method

 Revision 1.16  2003/02/06 17:41:04  billhorsman
 now uses imported logging

 Revision 1.15  2003/01/31 16:53:19  billhorsman
 checkstyle

 Revision 1.14  2003/01/28 11:47:08  billhorsman
 new isTrace() and made close() public

 Revision 1.13  2003/01/27 18:26:40  billhorsman
 refactoring of ProxyConnection and ProxyStatement to
 make it easier to write JDK 1.2 patch

 Revision 1.12  2002/12/19 00:08:36  billhorsman
 automatic closure of statements when a connection is closed

 Revision 1.11  2002/12/16 10:57:48  billhorsman
 add getDelegateStatement to allow access to the
 delegate JDBC driver's Statement

 Revision 1.10  2002/12/03 12:24:00  billhorsman
 fixed fatal sql exception

 Revision 1.9  2002/11/13 18:22:04  billhorsman
 fix for trace output

 Revision 1.8  2002/11/13 12:32:38  billhorsman
 now correctly logs trace messages even with verbose off

 Revision 1.7  2002/11/09 15:57:33  billhorsman
 finished off execute logging and listening

 Revision 1.6  2002/10/29 23:20:55  billhorsman
 logs execute time when debug is enabled and verbose is true

 Revision 1.5  2002/10/28 19:28:25  billhorsman
 checkstyle

 Revision 1.4  2002/10/28 08:20:23  billhorsman
 draft sql dump stuff

 Revision 1.3  2002/10/25 15:59:32  billhorsman
 made non-public where possible

 Revision 1.2  2002/10/17 15:29:18  billhorsman
 fixes so that equals() works

 Revision 1.1.1.1  2002/09/13 08:13:30  billhorsman
 new

 Revision 1.8  2002/08/24 19:57:15  billhorsman
 checkstyle changes

 Revision 1.7  2002/08/24 19:42:26  billhorsman
 new proxy stuff to work with JDK 1.4

 Revision 1.6  2002/07/10 16:14:47  billhorsman
 widespread layout changes and move constants into ProxoolConstants

 Revision 1.5  2002/07/02 11:19:08  billhorsman
 layout code and imports

 Revision 1.4  2002/06/28 11:19:47  billhorsman
 improved doc

*/
