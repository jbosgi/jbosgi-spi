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

// $Id$

import java.net.URL;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.osgi.spi.capability.Capability;
import org.jboss.osgi.spi.logging.LogEntryCache;
import org.jboss.osgi.spi.logging.RemoteLogReaderService;
import org.jboss.osgi.spi.management.MBeanProxy;
import org.jboss.osgi.spi.management.MBeanProxyException;
import org.jboss.osgi.spi.management.ManagedBundleMBean;
import org.jboss.osgi.spi.management.ManagedFrameworkMBean;
import org.jboss.osgi.spi.management.ManagedServiceReference;
import org.jboss.osgi.spi.testing.OSGiBundle;
import org.jboss.osgi.spi.testing.OSGiPackageAdmin;
import org.jboss.osgi.spi.testing.OSGiRuntime;
import org.jboss.osgi.spi.testing.OSGiServiceReference;
import org.jboss.osgi.spi.testing.OSGiTestHelper;
import org.jboss.osgi.spi.util.BundleDeploymentFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogReaderService;
import org.osgi.util.tracker.ServiceTracker;

/**
 * A remote implementation of the {@link OSGiRuntime}
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 25-Sep-2008
 */
public class RemoteRuntime extends OSGiRuntimeImpl
{
   private MBeanServerConnection mbeanServer;
   private ManagedFrameworkMBean managedFramework;

   // Needed for remote logging
   private EmbeddedRuntime embeddedRuntime;
   private RemoteLogCapability remoteLogCapability;

   public RemoteRuntime(OSGiTestHelper helper)
   {
      super(helper);
   }

   public OSGiBundle installBundle(String location) throws BundleException
   {
      String symbolicName = getSymbolicName(location);
      try
      {
         deploy(location);
         ManagedBundleMBean bundleMBean = getRemoteFramework().getBundle(symbolicName);
         RemoteBundle bundle = new RemoteBundle(this, bundleMBean, location);
         return registerBundle(location, bundle);
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (MBeanException ex)
      {
         Exception target = ex.getTargetException();
         if (target instanceof BundleException)
            throw (BundleException)target;
         
         throw new BundleException("Cannot install: " + location, target);
      }
      catch (Exception ex)
      {
         throw new BundleException("Cannot install: " + location, ex);
      }
   }
   
   @Override
   public void deploy(String location) throws Exception
   {
      URL archiveURL = getTestHelper().getTestArchiveURL(location);
      
      if (isBundleArchive(location))
         invokeDeployerService("deploy", archiveURL);
      else
         invokeMainDeployer("deploy", archiveURL);
   }

   @Override
   public void undeploy(String location) throws Exception
   {
      URL archiveURL = getTestHelper().getTestArchiveURL(location);
      
      if (isBundleArchive(location))
         invokeDeployerService("undeploy", archiveURL);
      else
         invokeMainDeployer("undeploy", archiveURL);
   }

   private boolean isBundleArchive(String location)
   {
      try
      {
         URL archiveURL = getTestHelper().getTestArchiveURL(location);
         BundleDeploymentFactory.createBundleDeployment(archiveURL);
         return true;
      }
      catch (BundleException ex)
      {
         return false;
      }
   }

   public OSGiBundle[] getBundles()
   {
      try
      {
         Set<ManagedBundleMBean> remoteBundles = getRemoteFramework().getBundles();
         Set<OSGiBundle> bundles = new HashSet<OSGiBundle>();
         for (ManagedBundleMBean remoteBundle : remoteBundles)
            bundles.add(new RemoteBundle(this, remoteBundle, null));

         OSGiBundle[] bundleArr = new OSGiBundle[bundles.size()];
         bundles.toArray(bundleArr);

         return bundleArr;
      }
      catch (Exception ex)
      {
         throw new IllegalStateException("Cannot obtain remote bundles", ex);
      }
   }

   public OSGiServiceReference getServiceReference(String clazz)
   {
      ManagedServiceReference manref = getRemoteFramework().getServiceReference(clazz);
      return manref != null ? new RemoteServiceReference(manref) : null;
   }

   public OSGiServiceReference[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException
   {
      OSGiServiceReference[] srefs = null;

      ManagedServiceReference[] manrefs = getRemoteFramework().getServiceReferences(clazz, filter);
      if (manrefs != null)
      {
         srefs = new OSGiServiceReference[manrefs.length];
         for (int i = 0; i < manrefs.length; i++)
            srefs[i] = new RemoteServiceReference(manrefs[i]);
      }

      return srefs;
   }

   public void startLogEntryTracking(final LogEntryCache logEntryCache)
   {
      super.startLogEntryTracking(logEntryCache);

      try
      {
         remoteLogCapability = new RemoteLogCapability();
         addCapability(remoteLogCapability);

         embeddedRuntime = (EmbeddedRuntime)getTestHelper().getEmbeddedRuntime();
         embeddedRuntime.addCapability(remoteLogCapability);

         // Track the RemoteLogReaderService to add the LogEntryCache as LogListener
         BundleContext context = embeddedRuntime.getBundleContext();
         ServiceTracker tracker = new ServiceTracker(context, RemoteLogReaderService.class.getName(), null)
         {
            @Override
            public Object addingService(ServiceReference sref)
            {
               LogReaderService logReaderService = (LogReaderService)super.addingService(sref);
               logReaderService.addLogListener(logEntryCache);
               setLogReaderService(logReaderService);
               return logReaderService;
            }
         };
         tracker.open();
      }
      catch (BundleException ex)
      {
         throw new IllegalStateException("Cannot start log entry tracking", ex);
      }
   }

   @Override
   public void stopLogEntryTracking()
   {
      if (remoteLogCapability != null)
      {
         removeCapability(remoteLogCapability);
         remoteLogCapability = null;
      }

      if (embeddedRuntime != null)
      {
         embeddedRuntime.shutdown();
         embeddedRuntime = null;
      }

      super.stopLogEntryTracking();
   }

   public MBeanServerConnection getMBeanServer()
   {
      if (mbeanServer == null)
      {
         try
         {
            InitialContext iniCtx = getInitialContext();
            mbeanServer = (MBeanServerConnection)iniCtx.lookup("jmx/invoker/RMIAdaptor");
         }
         catch (NamingException ex)
         {
            throw new IllegalStateException("Cannot obtain MBeanServerConnection", ex);
         }
      }
      return mbeanServer;
   }

   public OSGiPackageAdmin getPackageAdmin()
   {
      return new RemotePackageAdmin(this);
   }

   @Override
   public void shutdown()
   {
      super.shutdown();
      getPackageAdmin().refreshPackages(null);
   }

   private RemoteFramework getRemoteFramework()
   {
      try
      {
         if (managedFramework == null)
            managedFramework = MBeanProxy.get(ManagedFrameworkMBean.class, ManagedFrameworkMBean.MBEAN_MANAGED_FRAMEWORK, getMBeanServer());
      }
      catch (MBeanProxyException ex)
      {
         throw new RemoteFrameworkException(ex);
      }

      return new RemoteFramework()
      {
         public ManagedBundleMBean getBundle(String symbolicName)
         {
            ObjectName oname = managedFramework.getBundle(symbolicName);
            if (oname == null)
               throw new IllegalArgumentException("Cannot get remote bundle for: " + symbolicName);

            try
            {
               return MBeanProxy.get(ManagedBundleMBean.class, oname, getMBeanServer());
            }
            catch (MBeanProxyException ex)
            {
               throw new RemoteFrameworkException(ex);
            }
         }

         public Set<ManagedBundleMBean> getBundles()
         {
            Set<ManagedBundleMBean> remBundles = new HashSet<ManagedBundleMBean>();
            for (ObjectName bundleOName : managedFramework.getBundles())
            {
               try
               {
                  ManagedBundleMBean remBundle = MBeanProxy.get(ManagedBundleMBean.class, bundleOName, getMBeanServer());
                  remBundles.add(remBundle);
               }
               catch (MBeanProxyException ex)
               {
                  throw new RemoteFrameworkException(ex);
               }
            }
            return remBundles;
         }

         public ManagedServiceReference getServiceReference(String clazz)
         {
            return managedFramework.getServiceReference(clazz);
         }

         public ManagedServiceReference[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException
         {
            return managedFramework.getServiceReferences(clazz, filter);
         }
      };
   }

   private void invokeMainDeployer(String method, URL archiveURL) throws Exception
   {
      ObjectName oname = new ObjectName("jboss.system:service=MainDeployer");
      getMBeanServer().invoke(oname, method, new Object[] { archiveURL }, new String[] { "java.net.URL" });
   }

   /**
    * A copy of the RemoteLogCapability from the 
    * jboss-osgi-remotelog bundle, on which the SPI
    * should not have a dependency. 
    */
   class RemoteLogCapability extends Capability
   {
      public RemoteLogCapability()
      {
         super("org.jboss.osgi.remotelog.RemoteLogService");
         
         Properties props = getProperties();
         props.setProperty("org.jboss.osgi.service.remote.log.reader", "true");
         props.setProperty("org.jboss.osgi.service.remote.log.host", System.getProperty("jboss.bind.address", "localhost"));
         props.setProperty("org.jboss.osgi.service.remote.log.port", "5400");
         
         addBundle("bundles/jboss-osgi-remotelog.jar");
      }
   }}
