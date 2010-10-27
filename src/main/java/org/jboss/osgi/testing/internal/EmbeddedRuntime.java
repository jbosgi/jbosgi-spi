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


import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerFactory;

import org.jboss.logging.Logger;
import org.jboss.osgi.spi.capability.Capability;
import org.jboss.osgi.spi.framework.OSGiBootstrapProvider;
import org.jboss.osgi.spi.util.BundleInfo;
import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiRuntime;
import org.jboss.osgi.testing.OSGiRuntimeHelper;
import org.jboss.osgi.testing.OSGiServiceReference;
import org.jboss.osgi.vfs.VirtualFile;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * An embedded implementation of the {@link OSGiRuntime}
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 25-Sep-2008
 */
public class EmbeddedRuntime extends OSGiRuntimeImpl
{
   // Provide logging
   private static final Logger log = Logger.getLogger(EmbeddedRuntime.class);
   
   public EmbeddedRuntime(OSGiRuntimeHelper helper)
   {
      super(helper);
      OSGiBootstrapProvider bootProvider = helper.getBootstrapProvider();
      Framework framework = bootProvider.getFramework();
      try
      {
         log.debug("Framework start: " + framework);
         framework.start();
      }
      catch (BundleException ex)
      {
         throw new IllegalStateException("Cannot start framework", ex);
      }
   }

   public static MBeanServer getLocalMBeanServer()
   {
      MBeanServer mbeanServer = null;
   
      ArrayList<MBeanServer> serverArr = MBeanServerFactory.findMBeanServer(null);
      if (serverArr.size() > 1)
         log.warn("Multiple MBeanServer instances: " + serverArr);
   
      if (serverArr.size() > 0)
      {
         mbeanServer = serverArr.get(0);
         log.debug("Found MBeanServer: " + mbeanServer);
      }
   
      if (mbeanServer == null)
      {
         log.debug("No MBeanServer, create one ...");
         mbeanServer = MBeanServerFactory.createMBeanServer();
         log.debug("Created MBeanServer: " + mbeanServer);
      }
      return mbeanServer;
   }
   
   @Override
   OSGiBundle installBundleInternal(BundleInfo info) throws BundleException
   {
      try
      {
         VirtualFile rootFile = info.getRoot();
         BundleContext context = getSystemContext();
         Bundle auxBundle = context.installBundle(info.getLocation(), rootFile.openStream());
         return new EmbeddedBundle(this, auxBundle);
     }
      catch (IOException ex)
      {
         throw new BundleException("Cannot install bundle: " + info, ex);
      }
   }

   @Override
   public OSGiBundle[] getBundles()
   {
      List<OSGiBundle> absBundles = new ArrayList<OSGiBundle>();
      for (Bundle bundle : getSystemContext().getBundles())
      {
         absBundles.add(new EmbeddedBundle(this, bundle));
      }
      OSGiBundle[] bundleArr = new OSGiBundle[absBundles.size()];
      absBundles.toArray(bundleArr);
      return bundleArr;
   }

   @Override
   public OSGiBundle getBundle(long bundleId)
   {
      Bundle bundle = getSystemContext().getBundle(bundleId);
      return bundle != null ? new EmbeddedBundle(this, bundle) : null;
   }

   @Override
   public OSGiServiceReference getServiceReference(String clazz)
   {
      ServiceReference sref = getSystemContext().getServiceReference(clazz);
      return (sref != null ? new EmbeddedServiceReference(sref) : null);
   }

   @Override
   public OSGiServiceReference[] getServiceReferences(String clazz, String filter)
   {
      OSGiServiceReference[] retRefs = null;

      ServiceReference[] srefs;
      try
      {
         srefs = getSystemContext().getServiceReferences(clazz, filter);
      }
      catch (InvalidSyntaxException e)
      {
         throw new IllegalArgumentException("Invalid filter syntax: " + filter);
      }

      if (srefs != null)
      {
         retRefs = new OSGiServiceReference[srefs.length];
         for (int i = 0; i < srefs.length; i++)
            retRefs[i] = new EmbeddedServiceReference(srefs[i]);
      }
      return retRefs;
   }

   @Override
   public void addCapability(Capability capability) throws BundleException
   {
      // Copy the properties to the System props
      Map<String, String> props = capability.getSystemProperties();
      for (Entry<String, String> entry : props.entrySet())
      {
         String value = System.getProperty(entry.getKey());
         if (value == null)
            System.setProperty(entry.getKey(), entry.getValue());
      }
      super.addCapability(capability);
   }

   @Override
   public void refreshPackages(OSGiBundle[] bundles) throws IOException
   {
      BundleContext sysContext = getSystemContext();
      ServiceReference sref = sysContext.getServiceReference(PackageAdmin.class.getName());
      PackageAdmin packageAdmin = (PackageAdmin)sysContext.getService(sref);
      
      Bundle[] bundleArr = null;
      if (bundles != null)
      {
         bundleArr = new Bundle[bundles.length];
         for (int i = 0; i < bundles.length ; i++)
            bundleArr[i] = ((EmbeddedBundle)bundles[i]).getBundle();
      }
   
      final CountDownLatch latch = new CountDownLatch(1);
      FrameworkListener fl = new FrameworkListener()
      {
         @Override
         public void frameworkEvent(FrameworkEvent event)
         {
            if (event.getType() == FrameworkEvent.PACKAGES_REFRESHED)
               latch.countDown();
         }
      };

      try
      {
         sysContext.addFrameworkListener(fl);
         packageAdmin.refreshPackages(bundleArr);
         assertTrue(latch.await(10, TimeUnit.SECONDS));
      }
      catch (InterruptedException ex)
      {
         // ignore
      }
      finally
      {
         sysContext.removeFrameworkListener(fl);
      }
   }

   @Override
   public void shutdown()
   {
      OSGiBootstrapProvider bootProvider = getTestHelper().getBootstrapProvider();
      if (bootProvider != null)
      {
         super.shutdown();
         try
         {
            Framework framework = bootProvider.getFramework();
            log.debug("Framework stop: " + framework);
            framework.stop();
            framework.waitForStop(5000);
         }
         catch (Exception ex)
         {
            log.error("Cannot stop the framework", ex);
         }
         finally
         {
            getTestHelper().ungetBootstrapProvider();
         }
      }
   }

   @Override
   public MBeanServerConnection getMBeanServer()
   {
      return EmbeddedRuntime.getLocalMBeanServer();
   }

   @Override
   public boolean isRemoteRuntime()
   {
      return false;
   }
   
   BundleContext getSystemContext()
   {
      OSGiBootstrapProvider bootProvider = getTestHelper().getBootstrapProvider();
      Framework framework = bootProvider.getFramework();
      return framework.getBundleContext();
   }
}
