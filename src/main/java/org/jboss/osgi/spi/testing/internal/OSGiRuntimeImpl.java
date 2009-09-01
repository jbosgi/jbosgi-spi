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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.logging.Logger;
import org.jboss.osgi.spi.capability.Capability;
import org.jboss.osgi.spi.testing.OSGiBundle;
import org.jboss.osgi.spi.testing.OSGiRuntime;
import org.jboss.osgi.spi.testing.OSGiServiceReference;
import org.jboss.osgi.spi.testing.OSGiTestHelper;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.Version;

/**
 * An abstract implementation of the {@link OSGiRuntime}
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 25-Sep-2008
 */
public abstract class OSGiRuntimeImpl implements OSGiRuntime
{
   // Provide logging
   final Logger log = Logger.getLogger(OSGiRuntimeImpl.class);

   private OSGiTestHelper helper;
   private Map<String, OSGiBundle> bundles = new LinkedHashMap<String, OSGiBundle>();
   private List<Capability> capabilities = new ArrayList<Capability>();

   public OSGiRuntimeImpl(OSGiTestHelper helper)
   {
      this.helper = helper;
   }

   public OSGiTestHelper getTestHelper()
   {
      return helper;
   }

   public void addCapability(Capability capability) throws BundleException, InvalidSyntaxException
   {
      // Add dependent capabilies
      for (Capability dependency : capability.getDependencies())
         addCapability(dependency);

      // Check if the service provided by the capability exists already
      OSGiServiceReference[] srefs = getServiceReferences(capability.getServiceName(), capability.getFilter());
      if (srefs == null)
      {
         log.debug("Add capability: " + capability);

         for (String location : capability.getBundles())
         {
            String symName = getManifestEntry(location, Constants.BUNDLE_SYMBOLICNAME);
            if (bundles.get(location) == null && getBundle(symName, null) == null)
            {
               OSGiBundle bundle = installBundle(location);
               bundle.start();
            }
            else
            {
               log.debug("Skip bundle: " + location);
            }
         }
         capabilities.add(capability);
      }
      else
      {
         log.debug("Skip capability : " + capability);
      }
   }

   public void removeCapability(Capability capability)
   {
      if (capabilities.remove(capability))
      {
         log.debug("Remove capability : " + capability);

         List<String> bundleLocations = new ArrayList<String>(capability.getBundles());
         Collections.reverse(bundleLocations);

         for (String location : bundleLocations)
            failsafeUninstall(bundles.remove(location));
      }

      List<Capability> dependencies = new ArrayList<Capability>(capability.getDependencies());
      Collections.reverse(dependencies);

      // Remove dependent capabilities
      for (Capability dependency : dependencies)
         removeCapability(dependency);
   }

   public void shutdown()
   {
      log.debug("Start Shutdown");

      // Uninstall the registered bundles
      ArrayList<String> locations = new ArrayList<String>(bundles.keySet());
      Collections.reverse(locations);
      
      while (locations.size() > 0)
      {
         String location = locations.remove(0);
         failsafeUninstall(bundles.remove(location));
      }

      // Uninstall the capabilities
      Collections.reverse(capabilities);
      while (capabilities.size() > 0)
      {
         Capability capability = capabilities.get(0);
         removeCapability(capability);
      }

      log.debug("End Shutdown");
   }

   protected void deploy(String location) throws Exception
   {
      URL archiveURL = getTestHelper().getTestArchiveURL(location);
      invokeDeployerService("deploy", archiveURL);
   }

   protected void undeploy(String location) throws Exception
   {
      URL archiveURL = getTestHelper().getTestArchiveURL(location);
      invokeDeployerService("undeploy", archiveURL);
   }

   protected void invokeDeployerService(String method, URL archiveURL) throws Exception
   {
      ObjectName oname = new ObjectName("jboss.osgi:service=DeployerService");
      getMBeanServer().invoke(oname, method, new Object[] { archiveURL }, new String[] { "java.net.URL" });
   }

   public InitialContext getInitialContext() throws NamingException
   {
      return helper.getInitialContext();
   }

   public String getServerHost()
   {
      return helper.getServerHost();
   }

   public OSGiBundle getBundle(String symbolicName, String version)
   {
      OSGiBundle bundle = getBundle(symbolicName, version, false);
      return bundle;
   }

   protected OSGiBundle getBundle(String symbolicName, String versionStr, boolean mustExist)
   {
      OSGiBundle bundle = null;
      Version version = Version.parseVersion(versionStr);
      List<OSGiBundle> bundles = Arrays.asList(getBundles());
      for (OSGiBundle aux : bundles)
      {
         if (aux.getSymbolicName().equals(symbolicName))
         {
            if (versionStr == null || version.equals(aux.getVersion()))
            {
               bundle = aux;
               break;
            }
         }
      }
      
      if (bundle == null && mustExist == true)
         throw new IllegalStateException("Cannot obtain bundle: " + symbolicName + "-" + version + ". We have " + bundles);
      
      return bundle;
   }
   
   protected String getManifestEntry(String location, String key)
   {
      Manifest manifest = getManifest(location);
      Attributes attribs = manifest.getMainAttributes();
      String value = attribs.getValue(key);
      return value;
   }

   private Manifest getManifest(String location)
   {
      Manifest manifest;
      try
      {
         File archiveFile = getTestHelper().getTestArchiveFile(location);
         JarFile jarFile = new JarFile(archiveFile);
         manifest = jarFile.getManifest();
         jarFile.close();
      }
      catch (IOException ex)
      {
         throw new IllegalStateException("Cannot get manifest from: " + location);

      }
      return manifest;
   }

   OSGiBundle registerBundle(String location, OSGiBundle bundle)
   {
      if (bundle == null)
         throw new IllegalArgumentException("Cannot register null bundle for: " + location);
      
      bundles.put(location, bundle);
      return bundle;
   }

   void unregisterBundle(OSGiBundle bundle)
   {
      if (bundle == null)
         throw new IllegalArgumentException("Cannot unregister null bundle");
      
      if (bundles.containsValue(bundle))
      {
         Set<Entry<String, OSGiBundle>> entrySet = bundles.entrySet();
         for (Entry<String, OSGiBundle> entry : entrySet)
         {
            if (bundle.equals(entry.getValue()))
            {
               String key = entry.getKey();
               bundles.remove(key);
               break;
            }
         }
      }
   }

   private void failsafeUninstall(OSGiBundle bundle)
   {
      if (bundle != null)
      {
         try
         {
            if (bundle.getState() != Bundle.UNINSTALLED)
               bundle.uninstall();
         }
         catch (Exception ex)
         {
            log.warn("Cannot uninstall bundle: " + bundle, ex);
         }
      }
   }
}
