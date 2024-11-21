/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Responsible for prototyping connections for all pools
 * @version $Revision: 1.7 $, $Date: 2003/09/30 18:39:08 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8
 */
public class Prototyper {

    private ConnectionPool connectionPool;

    private Log log = LogFactory.getLog(Prototyper.class);

    private long connectionCount;

    private final Object lock = new Integer(1);

    private boolean sweepNeeded = true;

    /** This allows us to have a unique ID for each connection */
    private long nextConnectionId = 1;

    /**
     * Calling {@link #cancel} will set this to true and stop all
     * current prototyping immediately
     */
    private boolean cancel;

    /**
     * The number of connections currently being made (actually in
     * progress)
     */
    private int connectionsBeingMade;

    public Prototyper(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
        this.log = connectionPool.getLog();
    }

    protected boolean isSweepNeeded() {
        return sweepNeeded;
    }

    protected void triggerSweep() {
        sweepNeeded = true;
    }

    /**
     * Trigger prototyping immediately
     * @return true if something was prototyped
     */
    protected boolean sweep() {

        boolean somethingDone = false;
        try {

            while (!cancel && connectionPool.isConnectionPoolUp()) {

//                if (log.isDebugEnabled()) {
//                    log.debug("Prototyping");
//                }

                String reason = null;
                if (connectionCount >= getDefinition().getMaximumConnectionCount()) {
                    // We don't want to make any more that the maximum
                    break;
                } else if (connectionCount < getDefinition().getMinimumConnectionCount()) {
                    reason = "to achieve minimum of " + getDefinition().getMinimumConnectionCount();
                } else if (connectionPool.getAvailableConnectionCount() < getDefinition().getPrototypeCount()) {
                    reason = "to keep " + getDefinition().getPrototypeCount() + " available";
                } else {
                    // Nothing to do
                    break;
                }

                ProxyConnectionIF freshlyBuiltProxyConnection = null;
                try {
                    // If it has been shutdown then we should just stop now.
                    if (!connectionPool.isConnectionPoolUp()) {
                        break;
                    }
                    freshlyBuiltProxyConnection = buildConnection(ProxyConnection.STATUS_AVAILABLE, reason);
                    somethingDone = true;
                } catch (Throwable e) {
                    log.error("Prototype", e);
                    // If there's been an exception, perhaps we should stop
                    // prototyping for a while.  Otherwise if the database
                    // has problems we end up trying the connection every 2ms
                    // or so and then the log grows pretty fast.
                    break;
                    // Don't wory, we'll start again the next time the
                    // housekeeping thread runs.
                }
                if (freshlyBuiltProxyConnection == null) {
                    // That's strange. No double the buildConnection() method logged the
                    // error, but we should have build a connection here.
                }
            }
        } catch (Throwable t) {
            log.error("Unexpected error", t);
        }

        return somethingDone;
    }

    /**
     * Build a new connection
     * @param status the initial status it will be created as (this allows us
     * to create it as {@link ConnectionInfoIF#STATUS_ACTIVE ACTIVE} and avoid
     * another thread grabbing it before we can)
     * @param creator for log audit
     * @return the new connection
     */
    protected ProxyConnectionIF buildConnection(int status, String creator) throws SQLException, ProxoolException {

        long id = 0;
        synchronized (lock) {

            // Check that we are allowed to make another connection
            if (connectionCount >= getDefinition().getMaximumConnectionCount()) {
                throw new ProxoolException("ConnectionCount is " + connectionCount + ". Maximum connection count of "
                        + getDefinition().getMaximumConnectionCount() + " cannot be exceeded.");
            }

            checkSimultaneousBuildThrottle();

            connectionsBeingMade++;
            connectionCount++;
            id = nextConnectionId++;
        }


        ProxyConnection proxyConnection = null;
        Connection connection = null;

        try {
            proxyConnection = ProxyFactory.buildProxyConnection(id, connectionPool, status);
            connection = ProxyFactory.getConnection(proxyConnection);

            try {
                connectionPool.onBirth(connection);
            } catch (Exception e) {
                log.error("Problem during onBirth (ignored)", e);
            }
            connectionPool.addProxyConnection(proxyConnection);

            if (log.isDebugEnabled()) {
                StringBuffer out = new StringBuffer(connectionPool.displayStatistics());
                out.append(" - Connection #");
                out.append(proxyConnection.getId());
                if (getDefinition().isVerbose()) {
                    out.append(" (");
                    out.append(Integer.toHexString(proxyConnection.hashCode()));
                    out.append(")");
                }
                out.append(" created ");
                out.append(creator);
                out.append(" = ");
                out.append(ConnectionPool.getStatusDescription(proxyConnection.getStatus()));
                if (getDefinition().isVerbose()) {
                    out.append(" -> ");
                    out.append(getDefinition().getUrl());
                    out.append(" (");
                    out.append(Integer.toHexString(proxyConnection.getConnection().hashCode()));
                    out.append(") by thread ");
                    out.append(Thread.currentThread().getName());
                }
                log.debug(out);
            }
        } catch (SQLException e) {
            // log.error(displayStatistics() + " - Couldn't initialise connection #" + proxyConnection.getId() + ": " + e);
            throw e;
        } catch (RuntimeException e) {
            if (log.isDebugEnabled()) {
                log.debug("Prototyping problem", e);
            }
            throw e;
        } catch (Throwable t) {
            if (log.isDebugEnabled()) {
                log.debug("Prototyping problem", t);
            }
            throw new ProxoolException("Unexpected prototyping problem", t);
        } finally {
            synchronized (lock) {
                if (proxyConnection == null) {
                    // If there has been an exception then we won't be using this one and
                    // we need to decrement the counter
                    connectionCount--;
                }
                connectionsBeingMade--;
            }

        }

        return proxyConnection;
    }

    /**
     * This needs to be called _everytime_ a connection is removed.
     */
    protected void connectionRemoved() {
        connectionCount--;
    }

    /**
     * Checks whether we are currently already building too many connections
     * @throws SQLException if the throttle has been reached
     */
    protected void checkSimultaneousBuildThrottle() throws SQLException {
        // Check we aren't making too many simultaneously
        if (connectionsBeingMade > getDefinition().getSimultaneousBuildThrottle()) {
            throw new SQLException("We are already in the process of making " + connectionsBeingMade
                    + " connections and the number of simultaneous builds has been throttled to "
                    + getDefinition().getSimultaneousBuildThrottle());
        }
    }

    /**
     * The total number of  connections, including those being built right
     * now
     * @return connectionCount;
     */
    public long getConnectionCount() {
        return connectionCount;
    }

    /**
     * Utility method
     * @return definition
     */
    private ConnectionPoolDefinitionIF getDefinition() {
        return connectionPool.getDefinition();
    }

    /**
     * Cancel all current prototyping
     */
    public void cancel() {
        cancel = true;
    }

    /**
     * The alias of the pool we are prototyping for
     * @return alias
     */
    public String getAlias() {
        return getDefinition().getAlias();
    }

}


/*
 Revision history:
 $Log: Prototyper.java,v $
 Revision 1.7  2003/09/30 18:39:08  billhorsman
 New test-before-use, test-after-use and fatal-sql-exception-wrapper-class properties.

 Revision 1.6  2003/09/11 10:44:54  billhorsman
 Catch throwable not just exception during creation of connection
 (this will catch ClassNotFoundError too)

 Revision 1.5  2003/04/10 08:22:33  billhorsman
 removed some very frequent debug

 Revision 1.4  2003/03/11 14:51:52  billhorsman
 more concurrency fixes relating to snapshots

 Revision 1.3  2003/03/10 23:43:10  billhorsman
 reapplied checkstyle that i'd inadvertently let
 IntelliJ change...

 Revision 1.2  2003/03/10 15:26:47  billhorsman
 refactoringn of concurrency stuff (and some import
 optimisation)

 Revision 1.1  2003/03/05 18:42:33  billhorsman
 big refactor of prototyping and house keeping to
 drastically reduce the number of threads when using
 many pools

 */