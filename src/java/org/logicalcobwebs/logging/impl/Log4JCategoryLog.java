/*
 * $Header: /cvsroot/proxool/proxool/src/java/org/logicalcobwebs/logging/impl/Log4JCategoryLog.java,v 1.5 2003/10/20 07:42:03 chr32 Exp $
 * $Revision: 1.5 $
 * $Date: 2003/10/20 07:42:03 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.logicalcobwebs.logging.impl;

import org.logicalcobwebs.logging.Log;
import org.apache.log4j.Category;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Level;
import org.apache.log4j.Priority;

import java.util.Enumeration;

/**
 * <p>Implementation of {@link org.logicalcobwebs.logging.Log} that maps directly to a Log4J
 * <strong>Category</strong>.  Initial configuration of the corresponding
 * Category instances should be done in the usual manner, as outlined in
 * the Log4J documentation.</p>
 *
 * @author <a href="mailto:sanders@apache.org">Scott Sanders</a>
 * @author Rod Waldhoff
 * @author Robert Burrell Donkin
 * @version $Id: Log4JCategoryLog.java,v 1.5 2003/10/20 07:42:03 chr32 Exp $
 */
public final class Log4JCategoryLog implements Log {

    // ------------------------------------------------------------- Attributes

    /** The fully qualified name of the Log4JCategoryLog class. */
    private static final String FQCN = Log4JCategoryLog.class.getName ();

    private static boolean initialized = false;
    private static final String LAYOUT = "%r [%t] %p %c{2} %x - %m%n";

    /** Log to this category */
    private Category category = null;


    // ------------------------------------------------------------ Constructor

    public Log4JCategoryLog () {
        if (!initialized) {
            initialize ();
        }
    }

    /**
     * Base constructor
     */
    public Log4JCategoryLog (String name) {
        if (!initialized) {
            initialize ();
        }
        this.category = Category.getInstance (name);
    }

    /** For use with a log4j factory
     */
    public Log4JCategoryLog (Category category) {
        if (!initialized) {
            initialize ();
        }
        this.category = category;
    }


    // ---------------------------------------------------------- Implmentation

    private void initialize () {
        Category root = Category.getRoot ();
        Enumeration appenders = root.getAllAppenders ();
        if (appenders == null || !appenders.hasMoreElements ()) {
            // No config, set some defaults (consistent with
            // commons-logging patterns).
            ConsoleAppender app = new ConsoleAppender (new PatternLayout (LAYOUT),
                    ConsoleAppender.SYSTEM_ERR);
            app.setName ("commons-logging");

            root.addAppender (app);
            root.setLevel (Level.INFO);
        }
        initialized = true;
    }

    /**
     * Log a message to the Log4j Category with <code>TRACE</code> priority.
     * Currently logs to <code>DEBUG</code> level in Log4J.
     */
    public void trace (Object message) {
        category.log (FQCN, Priority.DEBUG, message, null);
    }

    /**
     * Log an error to the Log4j Category with <code>TRACE</code> priority.
     * Currently logs to <code>DEBUG</code> level in Log4J.
     */
    public void trace (Object message, Throwable t) {
        category.log (FQCN, Priority.DEBUG, message, t);
    }

    /**
     * Log a message to the Log4j Category with <code>DEBUG</code> priority.
     */
    public void debug (Object message) {
        category.log (FQCN, Priority.DEBUG, message, null);
    }

    /**
     * Log an error to the Log4j Category with <code>DEBUG</code> priority.
     */
    public void debug (Object message, Throwable t) {
        category.log (FQCN, Priority.DEBUG, message, t);
    }

    /**
     * Log a message to the Log4j Category with <code>INFO</code> priority.
     */
    public void info (Object message) {
        category.log (FQCN, Priority.INFO, message, null);
    }

    /**
     * Log an error to the Log4j Category with <code>INFO</code> priority.
     */
    public void info (Object message, Throwable t) {
        category.log (FQCN, Priority.INFO, message, t);
    }

    /**
     * Log a message to the Log4j Category with <code>WARN</code> priority.
     */
    public void warn (Object message) {
        category.log (FQCN, Priority.WARN, message, null);
    }

    /**
     * Log an error to the Log4j Category with <code>WARN</code> priority.
     */
    public void warn (Object message, Throwable t) {
        category.log (FQCN, Priority.WARN, message, t);
    }

    /**
     * Log a message to the Log4j Category with <code>ERROR</code> priority.
     */
    public void error (Object message) {
        category.log (FQCN, Priority.ERROR, message, null);
    }

    /**
     * Log an error to the Log4j Category with <code>ERROR</code> priority.
     */
    public void error (Object message, Throwable t) {
        category.log (FQCN, Priority.ERROR, message, t);
    }

    /**
     * Log a message to the Log4j Category with <code>FATAL</code> priority.
     */
    public void fatal (Object message) {
        category.log (FQCN, Priority.FATAL, message, null);
    }

    /**
     * Log an error to the Log4j Category with <code>FATAL</code> priority.
     */
    public void fatal (Object message, Throwable t) {
        category.log (FQCN, Priority.FATAL, message, t);
    }

    /**
     * Check whether the Log4j Category used is enabled for <code>DEBUG</code> priority.
     */
    public boolean isDebugEnabled () {
        return category.isDebugEnabled ();
    }

    /**
     * Check whether the Log4j Category used is enabled for <code>ERROR</code> priority.
     */
    public boolean isErrorEnabled () {
        return category.isEnabledFor (Priority.ERROR);
    }

    /**
     * Check whether the Log4j Category used is enabled for <code>FATAL</code> priority.
     */
    public boolean isFatalEnabled () {
        return category.isEnabledFor (Priority.FATAL);
    }

    /**
     * Check whether the Log4j Category used is enabled for <code>INFO</code> priority.
     */
    public boolean isInfoEnabled () {
        return category.isInfoEnabled ();
    }

    /**
     * Check whether the Log4j Category used is enabled for <code>TRACE</code> priority.
     * For Log4J, this returns the value of <code>isDebugEnabled()</code>
     */
    public boolean isTraceEnabled () {
        return category.isDebugEnabled ();
    }

    /**
     * Check whether the Log4j Category used is enabled for <code>WARN</code> priority.
     */
    public boolean isWarnEnabled () {
        return category.isEnabledFor (Priority.WARN);
    }
}

