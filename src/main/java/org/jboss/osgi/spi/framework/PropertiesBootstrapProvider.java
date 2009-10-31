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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.jboss.osgi.spi.NotImplementedException;
import org.jboss.osgi.spi.internal.StringPropertyReplacer;
import org.jboss.osgi.spi.util.ExportedPackageHelper;
import org.jboss.osgi.spi.util.ServiceLoader;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple properties based bootstrap provider
 * 
 * The PropertiesBootstrapProvider supports the following properties
 * 
 * <ul>
 * <li><b>org.jboss.osgi.spi.framework.autoInstall</b> - Bundles that need to be installed with the Framework automatically</li>
 * <li><b>org.jboss.osgi.spi.framework.autoStart</b> - Bundles that need to be started automatically</li>
 * </ul>
 * 
 * All other properties are passed on to configure the framework.
 * 
 * <pre>
 *    # Properties to configure the Framework
 *    org.osgi.framework.storage.clean=onFirstInit
 *    org.osgi.framework.system.packages=\
 *       org.osgi.framework; version=1.4, \
 *       javax.management
 *    
 *    # Bundles that need to be installed with the Framework automatically 
 *    org.jboss.osgi.spi.framework.autoInstall=\
 *       file://${test.archive.directory}/bundles/org.osgi.compendium.jar
 *    
 *    # Bundles that need to be started automatically 
 *    org.jboss.osgi.spi.framework.autoStart=\
 *       file://${test.archive.directory}/bundles/org.apache.felix.log.jar \
 *       file://${test.archive.directory}/bundles/jboss-osgi-logging.jar \
 *       file://${test.archive.directory}/bundles/jboss-osgi-common.jar \
 *       file://${test.archive.directory}/bundles/jboss-osgi-jmx.jar \
 *       file://${test.archive.directory}/bundles/jboss-osgi-microcontainer.jar
 * </pre>
 * 
 * @author thomas.diesler@jboss.com
 * @since 24-Apr-2009
 */
public class PropertiesBootstrapProvider implements OSGiBootstrapProvider
{
   // Provide logging
   final Logger log = LoggerFactory.getLogger(PropertiesBootstrapProvider.class);

   /** The default framework property: jboss.osgi.framework.properties */
   public static final String OSGI_FRAMEWORK_CONFIG = "jboss.osgi.framework.properties";
   /** The default framework config: jboss-osgi-framework.properties */
   public static final String DEFAULT_OSGI_FRAMEWORK_PROPERTIES = "jboss-osgi-framework.properties";

   /** Optional list of bundles that get installed automatically: org.jboss.osgi.spi.framework.autoInstall */
   public static final String PROP_OSGI_FRAMEWORK_AUTO_INSTALL = "org.jboss.osgi.spi.framework.autoInstall";
   /** Optional list of bundles that get started automatically: org.jboss.osgi.spi.framework.autoStart */
   public static final String PROP_OSGI_FRAMEWORK_AUTO_START = "org.jboss.osgi.spi.framework.autoStart";
   /** Optional path to extra properties: org.jboss.osgi.spi.framework.extra */
   public static final String PROP_OSGI_FRAMEWORK_EXTRA = "org.jboss.osgi.spi.framework.extra";

   private static Set<String> internalProps = new HashSet<String>();
   static
   {
      internalProps.add(PROP_OSGI_FRAMEWORK_AUTO_INSTALL);
      internalProps.add(PROP_OSGI_FRAMEWORK_AUTO_START);
      internalProps.add(PROP_OSGI_FRAMEWORK_EXTRA);
   }

   private Framework framework;
   private boolean configured;

   public void configure()
   {
      configure(System.getProperty(OSGI_FRAMEWORK_CONFIG, DEFAULT_OSGI_FRAMEWORK_PROPERTIES));
   }

   public void configure(URL urlConfig)
   {
      // Read the configuration properties
      final Map<String, Object> props = getBootstrapProperties(urlConfig);

      // Load the framework instance
      final Framework frameworkImpl = createFramework(props);
      framework = new FrameworkWrapper(frameworkImpl)
      {
         @Override
         public void start() throws BundleException
         {
            super.start();

            // Get system bundle context
            BundleContext context = getBundleContext();
            if (context == null)
               throw new FrameworkException("Cannot obtain system context");

            // Log the the framework packages
            ExportedPackageHelper packageHelper = new ExportedPackageHelper(context);
            packageHelper.logExportedPackages(frameworkImpl);

            // Init the the autoInstall URLs
            List<URL> autoInstall = getBundleURLs(props, PROP_OSGI_FRAMEWORK_AUTO_INSTALL);

            // Init the the autoStart URLs
            List<URL> autoStart = getBundleURLs(props, PROP_OSGI_FRAMEWORK_AUTO_START);

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
               Bundle bundle = context.installBundle(bundleURL.toString());
               long bundleId = bundle.getBundleId();
               log.info("Installed bundle [" + bundleId + "]: " + bundle.getSymbolicName());
               autoBundles.put(bundleURL, bundle);
            }

            // Start autoStart bundles
            for (URL bundleURL : autoStart)
            {
               Bundle bundle = autoBundles.get(bundleURL);
               if (bundle != null)
               {
                  bundle.start();
                  packageHelper.logExportedPackages(bundle);
                  log.info("Started bundle: " + bundle.getSymbolicName());
               }
            }
         }

         @Override
         public void stop() throws BundleException
         {
            // Unregister system services
            unregisterSystemServices(getBundleContext());
            
            super.stop();
         }
      };

      configured = true;
   }

   /** Overwrite to create the framework */
   protected Framework createFramework(Map<String, Object> properties)
   {
      FrameworkFactory factory = ServiceLoader.loadService(FrameworkFactory.class);
      Framework framework = factory.newFramework(properties);
      return framework;
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
   
   private List<URL> getBundleURLs(Map<String, Object> props, String key)
   {
      String bundleList = (String)props.get(key);
      if (bundleList == null)
         bundleList = "";

      List<URL> bundleURLs = new ArrayList<URL>();
      for (String bundle : bundleList.split("[, ]"))
      {
         if (bundle.trim().length() > 0)
         {
            URL installURL = toURL(bundle);
            bundleURLs.add(installURL);
         }
      }
      return bundleURLs;
   }

   private URL toURL(String path)
   {
      String realPath = StringPropertyReplacer.replaceProperties(path);
      try
      {
         URL pathURL = new URL(realPath);
         return pathURL;
      }
      catch (MalformedURLException ex)
      {
         throw new IllegalStateException("Invalid path: " + path, ex);
      }
   }

   public void configure(String resourceConfig)
   {
      URL urlConfig = Thread.currentThread().getContextClassLoader().getResource(resourceConfig);
      if (urlConfig == null)
         throw new IllegalStateException("Cannot find resource: " + resourceConfig);

      configure(urlConfig);
   }

   public void configure(InputStream streamConfig)
   {
      throw new NotImplementedException();
   }

   public Framework getFramework()
   {
      if (configured == false)
      {
         String defaultFrameworkProps = System.getProperty(OSGI_FRAMEWORK_CONFIG, DEFAULT_OSGI_FRAMEWORK_PROPERTIES);
         configure(defaultFrameworkProps);
      }
      return framework;
   }

   @SuppressWarnings("unchecked")
   private Map<String, Object> getBootstrapProperties(URL urlConfig)
   {
      Properties props = new Properties();
      try
      {
         InputStream inStream = urlConfig.openStream();
         props.load(inStream);
         inStream.close();
      }
      catch (IOException ex)
      {
         throw new IllegalStateException("Cannot load properties from: " + urlConfig, ex);
      }

      Map<String, Object> propMap = new HashMap<String, Object>();

      // Process property list
      Enumeration<String> keys = (Enumeration<String>)props.propertyNames();
      while (keys.hasMoreElements())
      {
         String key = keys.nextElement();
         String value = props.getProperty(key);

         // Replace property variables
         value = StringPropertyReplacer.replaceProperties(value);
         propMap.put(key, value);

         if (key.endsWith(".instance"))
         {
            try
            {
               String subkey = key.substring(0, key.lastIndexOf(".instance"));
               Object instance = Class.forName(value).newInstance();
               propMap.put(subkey, instance);
            }
            catch (Exception ex)
            {
               log.error("Cannot load " + key + "=" + value, ex);
            }
         }
      }

      // Merge optional extra properties
      String extraPropsValue = (String)propMap.get(PROP_OSGI_FRAMEWORK_EXTRA);
      if (extraPropsValue != null)
      {
         URL extraPropsURL = null;
         try
         {
            extraPropsURL = new URL(extraPropsValue);
         }
         catch (MalformedURLException e)
         {
            // ignore;
         }
         if (extraPropsURL == null)
         {
            File propsFile = new File(extraPropsValue);
            try
            {
               extraPropsURL = propsFile.toURL();
            }
            catch (MalformedURLException e)
            {
               // ignore;
            }
         }

         if (extraPropsURL == null)
            throw new IllegalStateException("Invalid properties URL: " + extraPropsValue);

         propMap.remove(PROP_OSGI_FRAMEWORK_EXTRA);
         Map<String, Object> extraProps = getBootstrapProperties(extraPropsURL);
         propMap.putAll(extraProps);
      }

      return propMap;
   }
}
