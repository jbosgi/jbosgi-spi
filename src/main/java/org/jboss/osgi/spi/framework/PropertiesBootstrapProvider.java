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
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.jboss.logging.Logger;
import org.jboss.osgi.spi.FrameworkException;
import org.jboss.osgi.spi.NotImplementedException;
import org.jboss.osgi.spi.internal.StringPropertyReplacer;
import org.jboss.osgi.spi.logging.ExportedPackageHelper;
import org.jboss.osgi.spi.util.ServiceLoader;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

/**
 * A simple properties based bootstrap provider
 * 
 * The PropertiesBootstrapProvider supports the following properties
 * 
 * <ul>
 * <li><b>org.jboss.osgi.spi.framework.impl</b> - The OSGiFramework implementation</li>
 * <li><b>org.jboss.osgi.spi.framework.autoInstall</b> - Bundles that need to be installed with the Framework automatically</li>
 * <li><b>org.jboss.osgi.spi.framework.autoStart</b> - Bundles that need to be started automatically</li>
 * </ul>
 * 
 * All other properties are passed on to configure the framework.
 * 
 * <pre>
 *    # The OSGiFramework implementation 
 *    org.jboss.osgi.spi.framework.impl=org.jboss.osgi.felix.framework.FelixIntegration
 *    
 *    # Properties to configure the Framework
 *    org.osgi.framework.storage.clean=onFirstInit
 *    org.osgi.framework.system.packages=\
 *       org.jboss.logging, \
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
   final Logger log = Logger.getLogger(PropertiesBootstrapProvider.class);
   
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
      configure(DEFAULT_OSGI_FRAMEWORK_PROPERTIES);
   }

   public void configure(URL urlConfig)
   {
      // Read the configuration properties
      final Map<String, Object> props = getBootstrapProperties(urlConfig);

      // Load the framework instance
      FrameworkFactory factory = ServiceLoader.loadService(FrameworkFactory.class);
      final Framework frameworkImpl = factory.newFramework(props);
      framework = new FrameworkDelegate(frameworkImpl)
      {
         @Override
         public void start() throws BundleException
         {
            super.start();
            
            // Get system bundle context
            BundleContext context = framework.getBundleContext();
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
                  //framework.stop();
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
                  //framework.stop();
                  throw new IllegalStateException("Cannot start bundle: " + bundleURL, ex);
               }
            }
         }
      };

      
      configured = true;
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
         propMap.put(key, StringPropertyReplacer.replaceProperties(value));
         
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
      String extraPropsValue = props.getProperty(PROP_OSGI_FRAMEWORK_EXTRA);
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
               extraPropsURL = propsFile.toURI().toURL();
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

   class FrameworkDelegate implements Framework
   {
      private Framework framework;

      FrameworkDelegate(Framework framework)
      {
         this.framework = framework;
      }

      @SuppressWarnings("unchecked")
      public Enumeration findEntries(String path, String filePattern, boolean recurse)
      {
         return framework.findEntries(path, filePattern, recurse);
      }

      public BundleContext getBundleContext()
      {
         return framework.getBundleContext();
      }

      public long getBundleId()
      {
         return framework.getBundleId();
      }

      public URL getEntry(String path)
      {
         return framework.getEntry(path);
      }

      @SuppressWarnings("unchecked")
      public Enumeration getEntryPaths(String path)
      {
         return framework.getEntryPaths(path);
      }

      @SuppressWarnings("unchecked")
      public Dictionary getHeaders()
      {
         return framework.getHeaders();
      }

      @SuppressWarnings("unchecked")
      public Dictionary getHeaders(String locale)
      {
         return framework.getHeaders(locale);
      }

      public long getLastModified()
      {
         return framework.getLastModified();
      }

      public String getLocation()
      {
         return framework.getLocation();
      }

      public ServiceReference[] getRegisteredServices()
      {
         return framework.getRegisteredServices();
      }

      public URL getResource(String name)
      {
         return framework.getResource(name);
      }

      @SuppressWarnings("unchecked")
      public Enumeration getResources(String name) throws IOException
      {
         return framework.getResources(name);
      }

      public ServiceReference[] getServicesInUse()
      {
         return framework.getServicesInUse();
      }

      @SuppressWarnings("unchecked")
      public Map getSignerCertificates(int signersType)
      {
         return framework.getSignerCertificates(signersType);
      }

      public int getState()
      {
         return framework.getState();
      }

      public String getSymbolicName()
      {
         return framework.getSymbolicName();
      }

      public Version getVersion()
      {
         return framework.getVersion();
      }

      public boolean hasPermission(Object permission)
      {
         return framework.hasPermission(permission);
      }

      public void init() throws BundleException
      {
         framework.init();
      }

      @SuppressWarnings("unchecked")
      public Class loadClass(String name) throws ClassNotFoundException
      {
         return framework.loadClass(name);
      }

      public void start() throws BundleException
      {
         framework.start();
      }

      public void start(int options) throws BundleException
      {
         framework.start(options);
      }

      public void stop() throws BundleException
      {
         framework.stop();
      }

      public void stop(int options) throws BundleException
      {
         framework.stop(options);
      }

      public void uninstall() throws BundleException
      {
         framework.uninstall();
      }

      public void update() throws BundleException
      {
         framework.update();
      }

      public void update(InputStream in) throws BundleException
      {
         framework.update(in);
      }

      public FrameworkEvent waitForStop(long timeout) throws InterruptedException
      {
         return framework.waitForStop(timeout);
      }
      
   }
}
