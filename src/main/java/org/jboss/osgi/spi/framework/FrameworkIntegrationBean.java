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
package org.jboss.osgi.spi.framework;

//$Id$

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.osgi.spi.FrameworkException;
import org.jboss.osgi.spi.logging.ExportedPackageHelper;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstraction of an OSGi Framework.
 * 
 * In addition to the standard {@link Framework} this implementation also
 * supports properties and initial bundle provisioning.
 * 
 * @author thomas.diesler@jboss.com
 * @since 23-Jan-2009
 */
public abstract class FrameworkIntegrationBean
{
   // Provide logging
   final Logger log = LoggerFactory.getLogger(FrameworkIntegrationBean.class);

   private Map<String, Object> properties = new HashMap<String, Object>();
   private List<URL> autoInstall = new ArrayList<URL>();
   private List<URL> autoStart = new ArrayList<URL>();

   private Framework framework;
   
   public Map<String, Object> getProperties()
   {
      return properties;
   }

   public void setProperties(Map<String, Object> props)
   {
      this.properties = props;
   }

   public List<URL> getAutoInstall()
   {
      return autoInstall;
   }

   public void setAutoInstall(List<URL> autoInstall)
   {
      this.autoInstall = autoInstall;
   }

   public List<URL> getAutoStart()
   {
      return autoStart;
   }

   public void setAutoStart(List<URL> autoStart)
   {
      this.autoStart = autoStart;
   }

   public Bundle getBundle()
   {
      assertFrameworkStart();
      return getFramework();
   }

   public BundleContext getBundleContext()
   {
      assertFrameworkStart();
      return getFramework().getBundleContext();
   }

   public Framework getFramework()
   {
      if (framework == null)
         framework = createFramework(properties);
      
      return framework;
   }

   public void create()
   {
      if (framework == null)
         framework = createFramework(properties);
   }

   /** Overwrite to create the framework */
   protected abstract Framework createFramework(Map<String, Object> properties);

   public void start()
   {
      // Create the Framework instance
      assertFrameworkCreate();
      
      // Start the System Bundle
      try
      {
         getFramework().start();
      }
      catch (BundleException ex)
      {
         throw new FrameworkException("Cannot start system bundle", ex);
      }
      
      // Get system bundle context
      BundleContext context = getFramework().getBundleContext();
      if (context == null)
         throw new FrameworkException("Cannot obtain system context");

      // Log the the framework packages
      ExportedPackageHelper packageHelper = new ExportedPackageHelper(context);
      packageHelper.logExportedPackages(getBundle());
      
      Map<URL, Bundle> autoBundles = new HashMap<URL, Bundle>();

      // Add the autoStart bundles to autoInstall
      for (URL bundleURL : autoStart)
      {
         autoInstall.add(bundleURL);
      }

      // Register system services
      registerSystemServices(context);
      
      // Install autoInstall bundles
      for (URL bundleURL : autoInstall)
      {
         try
         {
            Bundle bundle = context.installBundle(bundleURL.toString());
            long bundleId = bundle.getBundleId();
            log.info("Installed bundle [" + bundleId + "]: " + bundle.getSymbolicName());
            autoBundles.put(bundleURL, bundle);
         }
         catch (BundleException ex)
         {
            stop();
            throw new IllegalStateException("Cannot install bundle: " + bundleURL, ex);
         }
      }

      // Start autoStart bundles
      for (URL bundleURL : autoStart)
      {
         try
         {
            Bundle bundle = autoBundles.get(bundleURL);
            if (bundle != null)
            {
               bundle.start();
               packageHelper.logExportedPackages(bundle);
               log.info("Started bundle: " + bundle.getSymbolicName());
            }
         }
         catch (BundleException ex)
         {
            stop();
            throw new IllegalStateException("Cannot start bundle: " + bundleURL, ex);
         }
      }
   }

   public void stop()
   {
      Framework framework = getFramework();
      if (framework != null)
      {
         // Unregister system services
         unregisterSystemServices(getBundleContext());
         
         try
         {
            framework.stop();
            framework.waitForStop(5000);
            framework = null;
            log.debug("SystemBundle STOPPED");
         }
         catch (BundleException ex)
         {
            log.error("Cannot stop Framework", ex);
         }
         catch (InterruptedException ex)
         {
            log.error("Cannot stop Framework", ex);
         }
      }
   }

   /**
    * Overwrite to register system services before bundles get installed.
    */
   protected void registerSystemServices(BundleContext context)
   {
      // no default system services
   }
   
   /**
    * Overwrite to unregister system services before bundles get installed.
    */
   protected void unregisterSystemServices(BundleContext context)
   {
      // no default system services
   }
   
   private void assertFrameworkCreate()
   {
      if (getFramework() == null)
         create();
   }

   private void assertFrameworkStart()
   {
      assertFrameworkCreate();
      if ((getFramework().getState() & Bundle.ACTIVE) == 0)
         start();
   }
}