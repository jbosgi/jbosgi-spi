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

//$Id$

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.jboss.virtual.VFS;
import org.jboss.virtual.VFSUtils;
import org.jboss.virtual.VirtualFile;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;

/**
 * An abstraction of a bundle
 * 
 * @author thomas.diesler@jboss.com
 * @since 16-Oct-2009
 */
public class BundleInfo implements Serializable
{
   private static final long serialVersionUID = 2196462922955338109L;
   
   private VirtualFile root;
   private String location;
   private String symbolicName;
   private String version;
   
   private transient Manifest manifest;

   public static BundleInfo createBundleInfo(String location)
   {
      if (location == null)
         throw new IllegalArgumentException("Location cannot be null");
      
      // Try location as URL
      URL url = null;
      try
      {
         url = new URL(location);
      }
      catch (MalformedURLException ex)
      {
         // ignore
      }

      // Try location as File
      if (url == null)
      {
         try
         {
            File file = new File(location);
            if (file.exists())
               url = file.toURI().toURL();
         }
         catch (MalformedURLException e)
         {
            // ignore
         }
      }
      
      if (url == null)
         throw new IllegalArgumentException("Invalid bundle location: " + location);

      VirtualFile root;
      try
      {
         root = VFS.getRoot(url);
      }
      catch (IOException e)
      {
         throw new IllegalArgumentException("Invalid bundle location=" + url, e);
      }
      
      return new BundleInfo(root, location);
   }

   public static BundleInfo createBundleInfo(URL url)
   {
      if (url == null)
         throw new IllegalArgumentException("URL cannot be null");
      
      VirtualFile root;
      try
      {
         root = VFS.getRoot(url);
      }
      catch (IOException e)
      {
         throw new IllegalArgumentException("Invalid bundle location=" + url, e);
      }
      
      return new BundleInfo(root, url.toExternalForm());
   }
   
   public static BundleInfo createBundleInfo(VirtualFile root)
   {
      return new BundleInfo(root, null);
   }
   
   public static BundleInfo createBundleInfo(VirtualFile root, String location)
   {
      return new BundleInfo(root, location);
   }
   
   private BundleInfo(VirtualFile root, String location)
   {
      if (root == null)
         throw new IllegalArgumentException("VirtualFile cannot be null");
      
      this.root = root;
      
      // Derive the location from the root
      if (location == null)
      {
         try
         {
            location = root.toURL().toExternalForm();
         }
         catch (Exception e)
         {
            throw new IllegalStateException("Cannot obtain URL from: " + root);
         }
      }
      this.location = location;      

      symbolicName = getManifestHeader(Constants.BUNDLE_SYMBOLICNAME);
      if (symbolicName == null)
         throw new IllegalArgumentException("Cannot obtain Bundle-SymbolicName for: " + root);

      version = getManifestHeader(Constants.BUNDLE_VERSION);
      version = Version.parseVersion(version).toString();
   }

   /**
    * Get the manifest header for the given key.
    */
   public String getManifestHeader(String key)
   {
      Attributes attribs = getManifest().getMainAttributes();
      String value = attribs.getValue(key);
      return value;
   }

   /**
    * Get the bundle location
    */
   public String getLocation()
   {
      return location;
   }
   
   /**
    * Get the bundle root file
    */
   public VirtualFile getRoot()
   {
      return root;
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
   public Version getVersion()
   {
      return Version.parseVersion(version);
   }

   private Manifest getManifest()
   {
      if (manifest == null)
      {
         try
         {
            manifest = VFSUtils.getManifest(root);
         }
         catch (Exception ex)
         {
            throw new IllegalArgumentException("Cannot get manifest from: " + root, ex);
         }
      }
      return manifest;
   }
   
   private String toEqualString()
   {
      return "[" + symbolicName + "-" + version + ",url=" + location + "]";
   }
   
   @Override
   public boolean equals(Object obj)
   {
      if (!(obj instanceof BundleInfo))
         return false;
      
      BundleInfo other = (BundleInfo)obj;
      return toEqualString().equals(other.toEqualString());
   }

   @Override
   public int hashCode()
   {
      return toEqualString().hashCode();
   }

   @Override
   public String toString()
   {
      return toEqualString();
   }
}