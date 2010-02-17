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
package org.jboss.osgi.spi.management;

//$Id$

import java.io.File;
import java.util.Dictionary;

import javax.management.ObjectName;

import org.osgi.framework.BundleException;

/**
 * The managed view of an OSGi Bundle.
 * 
 * Bundles are registered under the name
 * 
 * jboss.osgi:name=[SymbolicName],id=[BundleId],version=[BundleVersion]
 * 
 * @author thomas.diesler@jboss.com
 * @since 04-Mar-2009
 */
public interface ManagedBundleMBean
{
   /**
    * Get the bundles object name.
    */
   ObjectName getObjectName();

   /**
    * Returns the bundle manifest headers
    */
   Dictionary<String, String> getHeaders();

   /**
    * Returns this bundle's Manifest headers and values localized to the specified locale.
    */
   Dictionary<String, String> getHeaders(String locale);
   
   /**
    * Returns the value of the specified property from the BundleContext.
    */
   String getProperty(String key);

   /**
    * Loads the specified class using this bundle's class loader. 
    * 
    * @param name The name of the class to load
    * @return The object name of the bundle that is wired to this bundle class loader and contains the class.
    * @throws ClassNotFoundException If no such class can be found or if this bundle is a fragment bundle
    */
   ObjectName loadClass(String name) throws ClassNotFoundException;
   
   /**
    * Returns a string encoded URL to the entry at the specified path in this bundle.
    * 
    * @param path The path name of the entry
    * @return A URL to the entry, or null if no entry could be found
    */
   String getEntry(String path);
   
   /**
    * Find the specified resource from this bundle's class loader. 
    * @param name The name of the resource.
    * @return A string encoded URL to the named resource, or null if the resource could not be found
    */
   String getResource(String name);
   
   /**
    * Creates a File object for a file in the persistent storage area provided for the bundle by the Framework.
    */
   File getDataFile(String filename);

   /**
    * Update this bundle.
    */
   void update() throws BundleException;
}