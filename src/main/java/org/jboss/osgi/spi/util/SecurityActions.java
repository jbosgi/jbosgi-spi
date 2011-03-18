/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.osgi.spi.util;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Privileged actions used by this package. 
 * No methods in this class are to be made public under any circumstances!
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 29-Oct-2010
 */
class SecurityActions {

    static String getSystemProperty(final String key, final String defaultValue) {
        if (System.getSecurityManager() == null) {
            String value = System.getProperty(key);
            return value != null ? value : defaultValue;
        } else {
            return AccessController.doPrivileged(new PrivilegedAction<String>() {
                public String run() {
                    String value = System.getProperty(key);
                    return value != null ? value : defaultValue;
                }
            });
        }
    }

    static void setSystemProperty(final String key, final String value) {
        if (System.getSecurityManager() == null) {
            System.setProperty(key, value);
        } else {
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                public Void run() {
                    System.setProperty(key, value);
                    return null;
                }
            });
        }
    }
}
