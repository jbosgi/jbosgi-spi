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
   private Map<String, BundleTuple> bundles = new LinkedHashMap<String, BundleTuple>();
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
         capability.install(this);
         
         // Start the capability bundles 
         capability.start(this);
         
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

         // Install the capability bundles 
         capability.stop(this);
         
         // Start the capability bundles 
         capability.uninstall(this);
         
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
      VirtualFile virtualFile = toVirtualFile(archive);
      BundleInfo info = BundleInfo.createBundleInfo(virtualFile);
      return installBundle(info);
   }

   public OSGiBundle installBundle(VirtualFile virtualFile) throws BundleException
   {
      BundleInfo info = BundleInfo.createBundleInfo(virtualFile);
      return installBundle(info);
   }

   private OSGiBundle installBundle(BundleInfo info) throws BundleException
   {
      log.debug("Install bundle: " + info);
      OSGiBundle bundle = installBundleInternal(info);
      bundles.put(info.getLocation(), new BundleTuple(info, bundle));
      return bundle;
   }
   
   abstract OSGiBundle installBundleInternal(BundleInfo info) throws BundleException;
   
   public void shutdown()
   {
      log.debug("Start Shutdown");

      // Uninstall the registered bundles
      ArrayList<String> locations = new ArrayList<String>(bundles.keySet());
      Collections.reverse(locations);

      while (locations.size() > 0)
      {
         String location = locations.remove(0);
         BundleTuple tuple = bundles.remove(location);
         tuple.uninstall();
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
      int fraction = 200;
      timeout = timeout / fraction;
      OSGiServiceReference sref = getServiceReference(clazz);
      while (sref == null && 0 < timeout--)
      {
         try
         {
            Thread.sleep(fraction);
         }
         catch (InterruptedException e)
         {
            // ignore
         }
         sref = getServiceReference(clazz);
      }
      return sref;
   }

   private OSGiBundle getBundle(String symbolicName, Version version, boolean mustExist)
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

   void uninstallBundle(OSGiBundle bundle)
   {
      if (bundle == null)
         throw new IllegalArgumentException("Cannot unregister null bundle");

      String location = bundle.getLocation();
      BundleTuple tuple = bundles.get(location);
      if (tuple != null)
         tuple.uninstall();
   }

   private VirtualFile toVirtualFile(Archive<?> archive) throws IOException, MalformedURLException
   {
      ZipExporter exporter = archive.as(ZipExporter.class);
      File target = File.createTempFile("archive_", ".jar");
      exporter.exportZip(target, true);
      target.deleteOnExit();
      
      return AbstractVFS.getRoot(target.toURI().toURL());
   }
   
   class BundleTuple
   {
      BundleInfo info;
      OSGiBundle bundle;
      
      BundleTuple(BundleInfo info, OSGiBundle bundle)
      {
         this.info = info;
         this.bundle = bundle;
      }

      public void uninstall()
      {
         OSGiRuntimeHelper.failsafeUninstall(bundle);
         info.close();
      }
   }
}
