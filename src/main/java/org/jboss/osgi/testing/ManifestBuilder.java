/*
* JBoss, Home of Professional Open Source
* Copyright 2006, JBoss Inc., and individual contributors as indicated
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.jboss.shrinkwrap.api.Asset;
import org.osgi.framework.Constants;

/**
 * A simple OSGi manifest builder.
 * 
 * @author thomas.diesler@jboss.com
 * @since 08-Mar-2010
 */
public final class ManifestBuilder implements Asset
{
   private StringWriter sw;
   private PrintWriter pw;
   private List<String> importPackages = new ArrayList<String>();
   private List<String> exportPackages = new ArrayList<String>();

   public static ManifestBuilder newInstance()
   {
      return new ManifestBuilder();
   }

   private ManifestBuilder()
   {
      sw = new StringWriter();
      pw = new PrintWriter(sw);
      pw.println(Attributes.Name.MANIFEST_VERSION + ": 1.0");
   }

   public ManifestBuilder addBundleManifestVersion(int version)
   {
      pw.println(Constants.BUNDLE_MANIFESTVERSION + ": " + version);
      return this;
   }

   public ManifestBuilder addBundleSymbolicName(String symbolicName)
   {
      pw.println(Constants.BUNDLE_SYMBOLICNAME + ": " + symbolicName);
      return this;
   }

   public ManifestBuilder addBundleActivator(String bundleActivator)
   {
      pw.println(Constants.BUNDLE_ACTIVATOR + ": " + bundleActivator);
      return this;
   }

   public ManifestBuilder addImportPackages(String... packages)
   {
      for (String aux : packages)
         importPackages.add(aux);

      return this;
   }

   public ManifestBuilder addExportPackages(String... packages)
   {
      for (String aux : packages)
         exportPackages.add(aux);

      return this;
   }

   public ManifestBuilder addManifestHeader(String key, String value)
   {
      pw.println(key + ": " + value);
      return this;
   }

   public Manifest getManifest()
   {
      if (exportPackages.size() > 0)
      {
         pw.print(Constants.EXPORT_PACKAGE + ": ");
         for (int i = 0; i < exportPackages.size(); i++)
         {
            if (i > 0)
               pw.print(",");
            
            pw.print(exportPackages.get(i));
         }
         pw.println();
      }
      
      if (importPackages.size() > 0)
      {
         pw.print(Constants.IMPORT_PACKAGE + ": ");
         for (int i = 0; i < importPackages.size(); i++)
         {
            if (i > 0)
               pw.print(",");
            
            pw.print(importPackages.get(i));
         }
         pw.println();
      }
      
      try
      {
         Manifest manifest = new Manifest(new ByteArrayInputStream(sw.toString().getBytes()));
         return manifest;
      }
      catch (IOException ex)
      {
         throw new IllegalStateException("Cannot create manifest", ex);
      }
   }

   @Override
   public InputStream openStream()
   {
      Manifest manifest = getManifest();
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      try
      {
         manifest.write(baos);
         return new ByteArrayInputStream(baos.toByteArray());
      }
      catch (IOException ex)
      {
         throw new IllegalStateException("Cannot provide manifest InputStream", ex);
      }
   }
}
