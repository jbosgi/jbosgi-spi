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

import java.io.Serializable;
import java.net.URL;

//$Id$

/**
 * An abstraction of a bundle deployment
 * 
 * @author thomas.diesler@jboss.com
 * @since 27-May-2009
 */
public class BundleDeployment implements Serializable
{
   private static final long serialVersionUID = 1L;
   
   private URL location;
   private String symbolicName;
   private String version;
   private int startLevel;
   private boolean autoStart;
   private Object metadata;

   public BundleDeployment(URL location, String symbolicName, String version)
   {
      if (location == null)
         throw new IllegalArgumentException("Location cannot be null");
      if (symbolicName == null)
         throw new IllegalArgumentException("Symbolic name cannot be null");
      
      if (version == null)
         version = "0.0.0";
      
      this.symbolicName = symbolicName;
      this.location = location;
      this.version = version;
   }

   /**
    * Get the bundle location
    */
   public URL getLocation()
   {
      return location;
   }

   /**
    * Get the bundle symbolic name
    */
   public String getSymbolicName()
   {
      return symbolicName;
   }

   /**
    * Get the bundle version
    */
   public String getVersion()
   {
      return version;
   }

   /**
    * Get the start level associated with this deployment
    */
   public int getStartLevel()
   {
      return startLevel;
   }

   /**
    * Set the start level associated with this deployment
    */
   public void setStartLevel(int startLevel)
   {
      this.startLevel = startLevel;
   }

   /**
    * Get the autostart flag associated with this deployment
    */
   public boolean isAutoStart()
   {
      return autoStart;
   }

   /**
    * Set the autostart flag associated with this deployment
    */
   public void setAutoStart(boolean autoStart)
   {
      this.autoStart = autoStart;
   }

   /**
    * Get extra meta data associated with this deployment
    */
   public Object getMetadata()
   {
      return metadata;
   }

   /**
    * Set extra meta data associated with this deployment
    */
   public void setMetadata(Object metadata)
   {
      this.metadata = metadata;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (!(obj instanceof BundleDeployment))
         return false;
      
      BundleDeployment other = (BundleDeployment)obj;
      return symbolicName.equals(other.symbolicName) && version.equals(other.version);
   }

   @Override
   public int hashCode()
   {
      return toString().hashCode();
   }

   @Override
   public String toString()
   {
      return "[" + symbolicName + "-" + version + ",url=" + location + "]";
   }
}