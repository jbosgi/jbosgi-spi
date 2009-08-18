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

import org.jboss.logging.Logger;
import org.jboss.osgi.spi.FrameworkException;
import org.jboss.osgi.spi.logging.ExportedPackageHelper;
import org.jboss.osgi.spi.util.ServiceLoader;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

/**
 * An abstraction of an OSGi Framework
 * 
 * @author thomas.diesler@jboss.com
 * @since 23-Jan-2009
 */
public class FrameworkIntegration
{
   // Provide logging
   final Logger log = Logger.getLogger(FrameworkIntegration.class);

   protected Map<String, Object> properties = new HashMap<String, Object>();
   protected List<URL> autoInstall = new ArrayList<URL>();
   protected List<URL> autoStart = new ArrayList<URL>();

   protected Framework framework;

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
      return framework;
   }

   public BundleContext getBundleContext()
   {
      return getBundle().getBundleContext();
   }

   public void create()
   {
      // Load the framework instance
      FrameworkFactory factory = ServiceLoader.loadService(FrameworkFactory.class);
      framework = factory.newFramework(properties);
   }

   public void start()
   {
      // Create the Framework instance
      assertFrameworkCreate();
      
      // Start the System Bundle
      try
      {
         framework.start();
      }
      catch (BundleException ex)
      {
         throw new FrameworkException("Cannot start system bundle", ex);
      }
      
      // Get system bundle context
      BundleContext context = framework.getBundleContext();
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
      if (framework != null)
      {
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

   private void assertFrameworkCreate()
   {
      if (framework == null)
         create();
   }

   private void assertFrameworkStart()
   {
      assertFrameworkCreate();
      if ((framework.getState() & Bundle.ACTIVE) == 0)
         start();
   }
}