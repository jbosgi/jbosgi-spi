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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerFactory;

import org.jboss.osgi.spi.capability.Capability;
import org.jboss.osgi.spi.framework.OSGiBootstrapProvider;
import org.jboss.osgi.spi.service.DeployerService;
import org.jboss.osgi.spi.testing.OSGiBundle;
import org.jboss.osgi.spi.testing.OSGiPackageAdmin;
import org.jboss.osgi.spi.testing.OSGiRuntime;
import org.jboss.osgi.spi.testing.OSGiServiceReference;
import org.jboss.osgi.spi.testing.OSGiTestHelper;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
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
   public EmbeddedRuntime(OSGiTestHelper helper)
   {
      super(helper);
   }

   public OSGiBundle installBundle(String location) throws BundleException
   {
      String symbolicName = getManifestEntry(location, Constants.BUNDLE_SYMBOLICNAME);
      String version = getManifestEntry(location, Constants.BUNDLE_VERSION);
      
      OSGiBundle bundle;
      
      BundleContext context = getBundleContext();
      URL bundleURL = getTestHelper().getTestArchiveURL(location);
      ServiceReference sref = context.getServiceReference(DeployerService.class.getName());
      if (sref != null)
      {
         DeployerService service = (DeployerService)context.getService(sref);
         service.deploy(bundleURL);
         bundle = getBundle(symbolicName, version);
      }
      else
      {
         Bundle auxBundle = context.installBundle(bundleURL.toExternalForm());
         bundle = new EmbeddedBundle(this, auxBundle);
      }
      return registerBundle(location, bundle);
   }

   public OSGiBundle[] getBundles()
   {
      List<OSGiBundle> absBundles = new ArrayList<OSGiBundle>();
      for (Bundle bundle : getBundleContext().getBundles())
      {
         absBundles.add(new EmbeddedBundle(this, bundle));
      }
      OSGiBundle[] bundleArr = new OSGiBundle[absBundles.size()];
      absBundles.toArray(bundleArr);
      return bundleArr;
   }
   
   public OSGiBundle getBundle(long bundleId)
   {
      Bundle bundle = getBundleContext().getBundle(bundleId);
      return bundle != null ? new EmbeddedBundle(this, bundle) : null;
   }
   
   public OSGiServiceReference getServiceReference(String clazz)
   {
      ServiceReference sref = getBundleContext().getServiceReference(clazz);
      return (sref != null ? new EmbeddedServiceReference(sref) : null);
   }

   public OSGiServiceReference[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException
   {
      OSGiServiceReference[] retRefs = null;
      ServiceReference[] srefs = getBundleContext().getServiceReferences(clazz, filter);
      if (srefs != null)
      {
         retRefs = new OSGiServiceReference[srefs.length];
         for(int i=0; i < srefs.length; i++)
            retRefs[i] = new EmbeddedServiceReference(srefs[i]);
      }
      return retRefs;
   }

   @Override
   public void addCapability(Capability capability) throws BundleException, InvalidSyntaxException
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
   public void shutdown()
   {
      OSGiBootstrapProvider bootProvider = getTestHelper().getBootstrapProvider();
      if (bootProvider != null)
      {
         super.shutdown();
         try
         {
            Framework framework = bootProvider.getFramework();
            framework.stop();
            framework.waitForStop(5000);
         }
         catch (Exception ex)
         {
            log.error("Cannot stop the framework", ex);
         }
      }
   }
   
   public BundleContext getBundleContext()
   {
      OSGiBootstrapProvider bootProvider = getTestHelper().getBootstrapProvider();
      Framework framework = bootProvider.getFramework();
      if (framework.getState() != Bundle.ACTIVE)
      {
         try
         {
            framework.start();
         }
         catch (BundleException ex)
         {
            throw new IllegalStateException("Cannot start framework", ex);
         }
      }
      return framework.getBundleContext();
   }

   @SuppressWarnings("unchecked")
   public MBeanServerConnection getMBeanServer()
   {
      ArrayList<MBeanServer> serverArr = MBeanServerFactory.findMBeanServer(null);
      if (serverArr.size() > 1)
         throw new IllegalStateException("Multiple MBeanServer instances not supported");
   
      MBeanServer server = null;
      if (serverArr.size() == 1)
         server = serverArr.get(0);
   
      if (server == null)
         server = MBeanServerFactory.createMBeanServer();
   
      return server;
   }

   public OSGiPackageAdmin getPackageAdmin()
   {
      BundleContext context = getBundleContext();
      ServiceReference sref = context.getServiceReference(PackageAdmin.class.getName());
      PackageAdmin packAdmin = (PackageAdmin)context.getService(sref);
      return new EmbeddedPackageAdmin(packAdmin);
   }
}
