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
package org.jboss.osgi.testing;

import java.io.File;
import java.net.URL;
import java.util.Dictionary;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

/**
 * An abstraction of an OSGi {@link Bundle}.
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 25-Sep-2008
 */
public interface OSGiBundle
{
   /**
    * Get the runtime associated with this bundle.
    */
   OSGiRuntime getRuntime();
   
   /**
    * Returns this bundle's unique identifier.
    */
   long getBundleId();
   
   /**
    * Returns the symbolic name of this bundle as specified by its Bundle-SymbolicName manifest header.
    */
   String getSymbolicName();

   /**
    * Returns the version of this bundle.
    */
   Version getVersion();
   
   /**
    * Returns this bundle's location.
    */
   String getLocation();
   
   /**
    * Returns this bundle's Manifest headers and values.
    */
   Dictionary<String, String> getHeaders();
   
   /**
    * Returns this bundle's Manifest headers and values localized to the specified locale.
    */
   Dictionary<String, String> getHeaders(String locale);
   
   /**
    * Returns this bundle's current state.
    */
   int getState();

   /**
    * Returns the value of the specified property.
    */
   String getProperty(String key);
   
   /**
    * Creates a File object for a file in the persistent storage area provided for the bundle by the Framework.
    */
   File getDataFile(String filename);

   /**
    * Loads the specified class using this bundle's class loader. 
    * 
    * @param name The name of the class to load
    * @return The OSGiBundle that is wired to this bundle class loader and contains the class.
    * @throws ClassNotFoundException If no such class can be found or if this bundle is a fragment bundle
    */
   OSGiBundle loadClass(String name) throws ClassNotFoundException;
   
   /**
    * Returns a URL to the entry at the specified path in this bundle.
    * 
    * @param path The path name of the entry
    * @return A URL to the entry, or null if no entry could be found
    */
   URL getEntry(String path);
   
   /**
    * Find the specified resource from this bundle's class loader. 
    * @param name The name of the resource.
    * @return A URL to the named resource, or null if the resource could not be found
    */
   URL getResource(String name);
   
   /**
    * Starts this bundle.
    */
   void start() throws BundleException;
   
   /**
    * Stops this bundle.
    */
   void stop() throws BundleException;
   
   /**
    * Uninstalls this bundle.
    */
   void uninstall() throws BundleException;
}
