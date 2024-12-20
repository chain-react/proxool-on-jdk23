/*
 * $Header: /cvsroot/proxool/proxool/src/java/org/logicalcobwebs/logging/LogSource.java,v 1.3 2003/03/11 00:02:06 billhorsman Exp $
 * $Revision: 1.3 $
 * $Date: 2003/03/11 00:02:06 $
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

package org.logicalcobwebs.logging;

import org.logicalcobwebs.logging.impl.NoOpLog;

import java.lang.reflect.Constructor;
import java.util.Hashtable;

/**
 * <p>Factory for creating {@link org.logicalcobwebs.logging.Log} instances.  Applications should call
 * the <code>makeNewLogInstance()</code> method to instantiate new instances
 * of the configured {@link org.logicalcobwebs.logging.Log} implementation class.</p>
 *
 * <p>By default, calling <code>getInstance()</code> will use the following
 * algorithm:</p>
 * <ul>
 * <li>If Log4J is available, return an instance of
 *     <code>org.logicalcobwebs.logging.impl.Log4JCategoryLog</code>.</li>
 * <li>If JDK 1.4 or later is available, return an instance of
 *     <code>org.logicalcobwebs.logging.impl.Jdk14Logger</code>.</li>
 * <li>Otherwise, return an instance of
 *     <code>org.logicalcobwebs.logging.impl.NoOpLog</code>.</li>
 * </ul>
 *
 * <p>You can change the default behavior in one of two ways:</p>
 * <ul>
 * <li>On the startup command line, set the system property
 *     <code>org.apache.commons.logging.log</code> to the name of the
 *     <code>org.logicalcobwebs.logging.Log</code> implementation class
 *     you want to use.</li>
 * <li>At runtime, call <code>LogSource.setLogImplementation()</code>.</li>
 * </ul>
 *
 * @deprecated Use {@link org.logicalcobwebs.logging.LogFactory} instead - The default factory
 *  implementation performs exactly the same algorithm as this class did
 *
 * @author Rod Waldhoff
 * @version $Id: LogSource.java,v 1.3 2003/03/11 00:02:06 billhorsman Exp $
 */
public class LogSource {

    // ------------------------------------------------------- Class Attributes

    private static Hashtable logs = new Hashtable ();

    /** Is log4j available (in the current classpath) */
    private static boolean log4jIsAvailable = false;

    /** Is JD 1.4 logging available */
    private static boolean jdk14IsAvailable = false;

    /** Constructor for current log class */
    private static Constructor logImplctor = null;


    // ----------------------------------------------------- Class Initializers

    static {

        // Is Log4J Available?
        try {
            if (null != Class.forName ("org.apache.log4j.Category")) {
                log4jIsAvailable = true;
            } else {
                log4jIsAvailable = false;
            }
        } catch (Throwable t) {
            log4jIsAvailable = false;
        }

        // Is JDK 1.4 Logging Available?
        try {
            if ((null != Class.forName ("java.util.logging.Logger"))
                && (null != Class.forName ("org.logicalcobwebs.logging.impl.Jdk14Logger"))) {
                jdk14IsAvailable = true;
            } else {
                jdk14IsAvailable = false;
            }
        } catch (Throwable t) {
            jdk14IsAvailable = false;
        }

        // Set the default Log implementation
        String name = null;
        try {
            name = System.getProperty ("org.apache.commons.logging.log");
            if (name == null) {
                name = System.getProperty ("org.logicalcobwebs.logging.Log");
            }
        } catch (Throwable t) {
            // oh well
        }
        if (name != null) {
            try {
                setLogImplementation (name);
            } catch (Throwable t) {
                try {
                    setLogImplementation
                            ("org.logicalcobwebs.logging.impl.NoOpLog");
                } catch (Throwable u) {
                    ;
                }
            }
        } else {
            try {
                if (log4jIsAvailable) {
                    setLogImplementation
                            ("org.logicalcobwebs.logging.impl.Log4JCategoryLog");
                } else if (jdk14IsAvailable) {
                    setLogImplementation
                            ("org.logicalcobwebs.logging.impl.Jdk14Logger");
                } else {
                    setLogImplementation
                            ("org.logicalcobwebs.logging.impl.NoOpLog");
                }
            } catch (Throwable t) {
                try {
                    setLogImplementation
                            ("org.logicalcobwebs.logging.impl.NoOpLog");
                } catch (Throwable u) {
                    ;
                }
            }
        }

    }


    // ------------------------------------------------------------ Constructor


    /** Don't allow others to create instances */
    private LogSource () {
    }


    // ---------------------------------------------------------- Class Methods


    /**
     * Set the log implementation/log implementation factory
     * by the name of the class.  The given class
     * must implement {@link org.logicalcobwebs.logging.Log}, and provide a constructor that
     * takes a single {@link java.lang.String} argument (containing the name
     * of the log).
     */
    public static void setLogImplementation (String classname) throws
            LinkageError, ExceptionInInitializerError,
            SecurityException {
        try {
            Class logclass = Class.forName (classname);
            Class[] argtypes = new Class[1];
            argtypes[0] = "".getClass ();
            logImplctor = logclass.getConstructor (argtypes);
        } catch (Throwable t) {
            logImplctor = null;
        }
    }

    /**
     * Set the log implementation/log implementation factory
     * by class.  The given class must implement {@link org.logicalcobwebs.logging.Log},
     * and provide a constructor that takes a single {@link java.lang.String}
     * argument (containing the name of the log).
     */
    public static void setLogImplementation (Class logclass) throws
            LinkageError, ExceptionInInitializerError,
            NoSuchMethodException, SecurityException {
        Class[] argtypes = new Class[1];
        argtypes[0] = "".getClass ();
        logImplctor = logclass.getConstructor (argtypes);
    }

    /** Get a <code>Log</code> instance by class name */
    public static Log getInstance (String name) {
        Log log = (Log) (logs.get (name));
        if (null == log) {
            log = makeNewLogInstance (name);
            logs.put (name, log);
        }
        return log;
    }

    /** Get a <code>Log</code> instance by class */
    public static Log getInstance (Class clazz) {
        return getInstance (clazz.getName ());
    }

    /**
     * Create a new {@link org.logicalcobwebs.logging.Log} implementation, based
     * on the given <i>name</i>.
     * <p>
     * The specific {@link org.logicalcobwebs.logging.Log} implementation returned
     * is determined by the value of the
     * <tt>org.apache.commons.logging.log</tt> property.
     * The value of <tt>org.apache.commons.logging.log</tt> may be set to
     * the fully specified name of a class that implements
     * the {@link org.logicalcobwebs.logging.Log} interface.  This class must also
     * have a public constructor that takes a single
     * {@link java.lang.String} argument (containing the <i>name</i>
     * of the {@link org.logicalcobwebs.logging.Log} to be constructed.
     * <p>
     * When <tt>org.apache.commons.logging.log</tt> is not set,
     * or when no corresponding class can be found,
     * this method will return a Log4JCategoryLog
     * if the log4j Category class is
     * available in the {@link org.logicalcobwebs.logging.LogSource}'s classpath, or a
     * Jdk14Logger if we are on a JDK 1.4 or later system, or
     * NoOpLog if neither of the above conditions is true.
     *
     * @param name the log name (or category)
     */
    public static Log makeNewLogInstance (String name) {

        Log log = null;
        try {
            Object[] args = new Object[1];
            args[0] = name;
            log = (Log) (logImplctor.newInstance (args));
        } catch (Throwable t) {
            log = null;
        }
        if (null == log) {
            log = new NoOpLog (name);
        }
        return log;

    }

    /**
     * Returns a {@link java.lang.String} array containing the names of
     * all logs known to me.
     */
    public static String[] getLogNames () {
        return (String[]) (logs.keySet ().toArray (new String[logs.size ()]));
    }

}

