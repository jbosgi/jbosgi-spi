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
package org.jboss.osgi.spi.testing;

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
public abstract class OSGiBundle
{
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
    * Returns this bundle's Manifest headers and values.
    */
   public abstract Dictionary<String, String> getHeaders();
   
   /**
    * Returns this bundle's current state.
    */
   public abstract int getState();

   /**
    * Returns the value of the specified property.
    */
   public abstract String getProperty(String key);

   /**
    * Starts this bundle.
    */
   public abstract void start() throws BundleException;
   
   /**
    * Stops this bundle.
    */
   public abstract void stop() throws BundleException;
   
   /**
    * Uninstalls this bundle.
    */
   public abstract void uninstall() throws BundleException;
   
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
      return "[" + getSymbolicName() + "," + getVersion() + "]";
   }
}
