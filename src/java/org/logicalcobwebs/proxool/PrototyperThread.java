/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

/**
 * Responsible for running {@link Prototyper#sweep sweep}. There
 * could be just one of the objects, or more.
 * @version $Revision: 1.4 $, $Date: 2003/04/10 08:23:55 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8
 */
public class PrototyperThread extends Thread {

    private static final ThreadGroup PROTOTYPER_THREAD_GROUP = new ThreadGroup("PROTOTYPER_THREAD_GROUP");

    private static final Log LOG = LogFactory.getLog(PrototyperThread.class);

    public PrototyperThread(String name) {
        super(PROTOTYPER_THREAD_GROUP, name);
        setDaemon(true);
    }

    public void run() {

        while (true) {
            int sweptCount = 0;
            while (PrototyperController.isKeepSweeping()) {
                PrototyperController.sweepStarted();
                ConnectionPool[] cps = ConnectionPoolManager.getInstance().getConnectionPools();
                for (int i = 0; i < cps.length; i++) {
                    Prototyper p = cps[i].getPrototyper();
                    try {
                        cps[i].acquirePrimaryReadLock();
                        if (cps[i].isConnectionPoolUp() && p.isSweepNeeded()) {
                            p.sweep();
                            sweptCount++;
                        }
                    } catch (InterruptedException e) {
                        LOG.error("Couldn't acquire primary read lock", e);
                    } finally {
                        cps[i].releasePrimaryReadLock();
                    }
                }
            }
//            if (LOG.isDebugEnabled()) {
//                LOG.debug("Swept " + sweptCount + " pools");
//            }

            doWait();
        }
    }

    private synchronized void doWait() {
        try {
            wait();
        } catch (InterruptedException e) {
            LOG.debug("Expected interruption of sleep");
        }
    }

    protected synchronized void doNotify() {
        notify();
    }

}


/*
 Revision history:
 $Log: PrototyperThread.java,v $
 Revision 1.4  2003/04/10 08:23:55  billhorsman
 removed very frequent debug

 Revision 1.3  2003/03/10 23:43:12  billhorsman
 reapplied checkstyle that i'd inadvertently let
 IntelliJ change...

 Revision 1.2  2003/03/10 15:26:48  billhorsman
 refactoringn of concurrency stuff (and some import
 optimisation)

 Revision 1.1  2003/03/05 18:42:33  billhorsman
 big refactor of prototyping and house keeping to
 drastically reduce the number of threads when using
 many pools

 */