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
package org.jboss.osgi.testing.internal;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
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

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.logging.Logger;
import org.jboss.osgi.jmx.BundleStateMBeanExt;
import org.jboss.osgi.jmx.FrameworkMBeanExt;
import org.jboss.osgi.jmx.MBeanProxy;
import org.jboss.osgi.jmx.ObjectNameFactory;
import org.jboss.osgi.jmx.PackageStateMBeanExt;
import org.jboss.osgi.jmx.ServiceStateMBeanExt;
import org.jboss.osgi.spi.capability.Capability;
import org.jboss.osgi.spi.util.BundleInfo;
import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiRuntime;
import org.jboss.osgi.testing.OSGiRuntimeHelper;
import org.jboss.osgi.testing.OSGiServiceReference;
import org.jboss.osgi.vfs.AbstractVFS;
import org.jboss.osgi.vfs.VirtualFile;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;
import org.osgi.jmx.framework.BundleStateMBean;
import org.osgi.jmx.framework.FrameworkMBean;
import org.osgi.jmx.framework.PackageStateMBean;
import org.osgi.jmx.framework.ServiceStateMBean;

/**
 * An abstract implementation of the {@link OSGiRuntime}
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 25-Sep-2008
 */
public abstract class OSGiRuntimeImpl implements OSGiRuntime
{
   // Provide logging
   private static final Logger log = Logger.getLogger(OSGiRuntimeImpl.class);

   private OSGiRuntimeHelper helper;
   private Map<String, OSGiBundle> bundles = new LinkedHashMap<String, OSGiBundle>();
   private List<Capability> capabilities = new ArrayList<Capability>();
   
   private FrameworkMBean frameworkState;
   private BundleStateMBean bundleState;
   private ServiceStateMBean serviceState;
   private PackageStateMBean packageState;

   public OSGiRuntimeImpl(OSGiRuntimeHelper helper)
   {
      this.helper = helper;
   }

   public OSGiRuntimeHelper getTestHelper()
   {
      return helper;
   }

   public void addCapability(Capability capability) throws BundleException
   {
      // Add dependent capabilies
      for (Capability dependency : capability.getDependencies())
         addCapability(dependency);

      OSGiServiceReference[] srefs = null;

      // Check if the service provided by the capability exists already
      String serviceName = capability.getServiceName();
      if (serviceName != null)
         srefs = getServiceReferences(serviceName, capability.getFilter());

      if (srefs == null || srefs.length == 0)
      {
         log.debug("Add capability: " + capability);

         // Install the capability bundles 
         List<OSGiBundle> installed = new ArrayList<OSGiBundle>();
         for (BundleInfo info : capability.getBundles())
         {
            String location = info.getLocation();
            String symName = info.getSymbolicName();
            Version version = info.getVersion();
            if (bundles.get(location) == null && getBundle(symName, version) == null)
            {
               OSGiBundle bundle = installBundle(location);
               installed.add(bundle);
            }
            else
            {
               log.debug("Skip bundle: " + location);
            }
         }

         // Start the capability bundles
         for (OSGiBundle bundle : installed)
         {
            bundle.start();
         }
         capabilities.add(capability);
      }
      else
      {
         log.debug("Skip capability: " + capability);
      }
   }

   public void removeCapability(Capability capability)
   {
      if (capabilities.remove(capability))
      {
         log.debug("Remove capability : " + capability);

         List<BundleInfo> bundleInfos = new ArrayList<BundleInfo>(capability.getBundles());
         Collections.reverse(bundleInfos);

         for (BundleInfo info : bundleInfos)
         {
            OSGiBundle bundle = bundles.get(info.getLocation());
            failsafeStop(bundle);
         }
         
         for (BundleInfo info : bundleInfos)
         {
            OSGiBundle bundle = bundles.remove(info.getLocation());
            failsafeUninstall(bundle);
         }
      }

      List<Capability> dependencies = new ArrayList<Capability>(capability.getDependencies());
      Collections.reverse(dependencies);

      // Remove dependent capabilities
      for (Capability dependency : dependencies)
         removeCapability(dependency);
   }

   public OSGiBundle installBundle(String location) throws BundleException
   {
      BundleInfo info = BundleInfo.createBundleInfo(location);
      return installBundle(info);
   }

   public OSGiBundle installBundle(Archive<?> archive) throws BundleException, IOException
   {
      VirtualFile file = toVirtualFile(archive);
      return installBundle(file);
   }

   public OSGiBundle installBundle(VirtualFile virtualFile) throws BundleException
   {
      BundleInfo info = BundleInfo.createBundleInfo(virtualFile);
      return installBundle(info);
   }

   abstract OSGiBundle installBundle(BundleInfo info) throws BundleException;
   
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

   public FrameworkMBean getFrameworkMBean() throws IOException
   {
      if (frameworkState == null)
      {
         ObjectName objectName = ObjectNameFactory.create(FrameworkMBeanExt.OBJECTNAME);
         MBeanServerConnection server = getMBeanServer();
         if (server.isRegistered(objectName))
         {
            frameworkState = MBeanProxy.get(server, objectName, FrameworkMBeanExt.class);
         }
         else
         {
            objectName = ObjectNameFactory.create(FrameworkMBean.OBJECTNAME);
            frameworkState = MBeanProxy.get(server, objectName, FrameworkMBean.class);
         }
      }
      return frameworkState;
   }

   public BundleStateMBean getBundleStateMBean() throws IOException
   {
      if (bundleState == null)
      {
         MBeanServerConnection server = getMBeanServer();
         ObjectName objectName = ObjectNameFactory.create(BundleStateMBeanExt.OBJECTNAME);
         if (server.isRegistered(objectName))
         {
            bundleState = MBeanProxy.get(server, objectName, BundleStateMBeanExt.class);
         }
         else
         {
            objectName = ObjectNameFactory.create(BundleStateMBean.OBJECTNAME);
            bundleState = MBeanProxy.get(server, objectName, BundleStateMBean.class);
         }
      }
      return bundleState;
   }

   public PackageStateMBean getPackageStateMBean() throws IOException
   {
      if (packageState == null)
      {
         MBeanServerConnection server = getMBeanServer();
         ObjectName objectName = ObjectNameFactory.create(PackageStateMBeanExt.OBJECTNAME);
         if (server.isRegistered(objectName))
         {
            packageState = MBeanProxy.get(server, objectName, PackageStateMBeanExt.class);
         }
         else
         {
            objectName = ObjectNameFactory.create(PackageStateMBean.OBJECTNAME);
            packageState = MBeanProxy.get(server, objectName, PackageStateMBean.class);
         }
      }
      return packageState;
   }

   public ServiceStateMBean getServiceStateMBean() throws IOException
   {
      if (serviceState == null)
      {
         MBeanServerConnection server = getMBeanServer();
         ObjectName objectName = ObjectNameFactory.create(ServiceStateMBeanExt.OBJECTNAME);
         if (server.isRegistered(objectName))
         {
            serviceState = MBeanProxy.get(server, objectName, ServiceStateMBeanExt.class);
         }
         else
         {
            objectName = ObjectNameFactory.create(ServiceStateMBean.OBJECTNAME);
            serviceState = MBeanProxy.get(server, objectName, ServiceStateMBean.class);
         }
      }
      return serviceState;
   }

   public InitialContext getInitialContext() throws NamingException
   {
      return helper.getInitialContext();
   }

   public String getServerHost()
   {
      return helper.getServerHost();
   }

   public OSGiBundle getBundle(String symbolicName, Version version)
   {
      OSGiBundle bundle = getBundle(symbolicName, version, false);
      return bundle;
   }

   public OSGiServiceReference getServiceReference(String clazz, long timeout)
   {
      int fraktion = 200;
      timeout = timeout / fraktion;
      OSGiServiceReference sref = getServiceReference(clazz);
      while (sref == null && 0 < timeout--)
      {
         try
         {
            Thread.sleep(fraktion);
         }
         catch (InterruptedException e)
         {
            // ignore
         }
         sref = getServiceReference(clazz);
      }
      return sref;
   }

   OSGiBundle getBundle(String symbolicName, Version version, boolean mustExist)
   {
      OSGiBundle bundle = null;
      List<OSGiBundle> bundles = Arrays.asList(getBundles());
      for (OSGiBundle aux : bundles)
      {
         if (aux.getSymbolicName().equals(symbolicName))
         {
            if (version == null || version.equals(aux.getVersion()))
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

   String getManifestEntry(String location, String key)
   {
      Manifest manifest = getManifest(location);
      Attributes attribs = manifest.getMainAttributes();
      String value = attribs.getValue(key);
      return value;
   }

   Manifest getManifest(String location)
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

   VirtualFile toVirtualFile(Archive<?> archive) throws IOException, MalformedURLException
   {
      ZipExporter exporter = archive.as(ZipExporter.class);
      File target = File.createTempFile("archive_", ".jar");
      exporter.exportZip(target, true);
      target.deleteOnExit();
      
      return AbstractVFS.getRoot(target.toURI().toURL());
   }
   
   private void failsafeStop(OSGiBundle bundle)
   {
      if (bundle != null)
      {
         try
         {
            bundle.stop();
         }
         catch (Exception ex)
         {
            log.warn("Cannot stop bundle: " + bundle, ex);
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
