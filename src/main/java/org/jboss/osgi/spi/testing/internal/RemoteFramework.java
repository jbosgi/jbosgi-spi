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
package org.jboss.osgi.spi.testing.internal;

//$Id$

import java.util.Set;

import org.jboss.osgi.spi.management.ManagedBundleMBean;
import org.jboss.osgi.spi.management.ManagedServiceReference;

/**
 * The supported functionality of a remote OSGi Framework
 * 
 * @author thomas.diesler@jboss.com
 * @since 04-Mar-2009
 */
public interface RemoteFramework
{
   /**
    * Get the list of all installed bundles
    */
   Set<ManagedBundleMBean> getBundles();
   
   /**
    * Get the installed bundle 
    */
   ManagedBundleMBean getBundle(String symbolicName, String version);

   /**
    * Get the installed bundle by id
    */
   ManagedBundleMBean getBundle(long bundleId);

   /**
    * Returns a ServiceReference object for a service that implements and was registered 
    * under the specified class.
    */
   ManagedServiceReference getServiceReference(String clazz);
   
   /**
    * Returns an array of ManagedServiceReference objects. 
    * The returned array of ManagedServiceReference objects contains services 
    * that were registered under the specified class, match the specified filter criteria, 
    * and the packages for the class names under which the services were registered.
    */
   ManagedServiceReference[] getServiceReferences(String clazz, String filter);
}