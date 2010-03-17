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

import org.jboss.logging.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

/**
 * An abstraction of an OSGi {@link Bundle}.
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 25-Sep-2008
 */
public abstract class OSGiBundle
{
   // Provide logging
   private static final Logger log = Logger.getLogger(OSGiBundle.class);
   /**
    * Get the runtime associated with this bundle.
    */
   public abstract OSGiRuntime getRuntime();
   
   /**
    * Returns this bundle's unique identifier.
    */
   public abstract long getBundleId();
   
   /**
    * Returns the symbolic name of this bundle as specified by its Bundle-SymbolicName manifest header.
    */
   public abstract String getSymbolicName();

   /**
    * Returns the version of this bundle.
    */
   public abstract Version getVersion();
   
   /**
    * Returns this bundle's location.
    */
   public abstract String getLocation();
   
   /**
    * Returns this bundle's Manifest headers and values.
    */
   public abstract Dictionary<String, String> getHeaders();
   
   /**
    * Returns this bundle's Manifest headers and values localized to the specified locale.
    */
   public abstract Dictionary<String, String> getHeaders(String locale);
   
   /**
    * Returns this bundle's current state.
    */
   public abstract int getState();

   /**
    * Returns the value of the specified property.
    */
   public abstract String getProperty(String key);
   
   /**
    * Creates a File object for a file in the persistent storage area provided for the bundle by the Framework.
    */
   public abstract File getDataFile(String filename);

   /**
    * Loads the specified class using this bundle's class loader. 
    * 
    * @param name The name of the class to load
    * @return The OSGiBundle that is wired to this bundle class loader and contains the class.
    * @throws ClassNotFoundException If no such class can be found or if this bundle is a fragment bundle
    */
   public abstract OSGiBundle loadClass(String name) throws ClassNotFoundException;
   
   /**
    * Returns a URL to the entry at the specified path in this bundle.
    * 
    * @param path The path name of the entry
    * @return A URL to the entry, or null if no entry could be found
    */
   public abstract URL getEntry(String path);
   
   /**
    * Find the specified resource from this bundle's class loader. 
    * @param name The name of the resource.
    * @return A URL to the named resource, or null if the resource could not be found
    */
   public abstract URL getResource(String name);
   
   /**
    * Starts this bundle.
    */
   public void start() throws BundleException
   {
      log.debug("Start bundle: " + this);
      startInternal();
   }
   
   protected abstract void startInternal() throws BundleException;
   
   /**
    * Stops this bundle.
    */
   public void stop() throws BundleException
   {
      log.debug("Stop bundle: " + this);
      stopInternal();
   }
   
   protected abstract void stopInternal() throws BundleException;
   
   /**
    * Uninstalls this bundle.
    */
   public void uninstall() throws BundleException
   {
      log.debug("Uninstall bundle: " + this);
      uninstallInternal();
   }
   
   protected abstract void uninstallInternal() throws BundleException;
   
   /**
    * Return true if symbolic name and version are equal
    */
   public boolean equals(Object obj)
   {
      if ((obj instanceof OSGiBundle) == false)
         return false;
      
      OSGiBundle other = (OSGiBundle)obj;
      
      boolean isEqual =  getSymbolicName().equals(other.getSymbolicName());
      isEqual = isEqual && getVersion().equals(other.getVersion());
      return isEqual;
   }

   /**
    * Returns the hash code for this bundle. 
    */
   public int hashCode()
   {
      return toString().hashCode();
   }

   /**
    * Returns the string representation of this bundle 
    */
   public String toString()
   {
      return "[" + getSymbolicName() + ":" + getVersion() + "]";
   }
}
