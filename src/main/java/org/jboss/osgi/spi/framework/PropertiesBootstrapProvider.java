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
   /** The default framework property: jboss.osgi.framework.properties */
   public static final String OSGI_FRAMEWORK_CONFIG = "jboss.osgi.framework.properties";
   /** The default framework config: jboss-osgi-framework.properties */
   public static final String DEFAULT_OSGI_FRAMEWORK_PROPERTIES = "jboss-osgi-framework.properties";

   /** The OSGi framework integration class: org.jboss.osgi.spi.framework.impl */
   public static final String PROP_OSGI_FRAMEWORK_IMPL = "org.jboss.osgi.spi.framework.impl";
   /** Optional list of bundles that get installed automatically: org.jboss.osgi.spi.framework.autoInstall */
   public static final String PROP_OSGI_FRAMEWORK_AUTO_INSTALL = "org.jboss.osgi.spi.framework.autoInstall";
   /** Optional list of bundles that get started automatically: org.jboss.osgi.spi.framework.autoStart */
   public static final String PROP_OSGI_FRAMEWORK_AUTO_START = "org.jboss.osgi.spi.framework.autoStart";
   /** Optional path to extra properties: org.jboss.osgi.spi.framework.extra */
   public static final String PROP_OSGI_FRAMEWORK_EXTRA = "org.jboss.osgi.spi.framework.extra";

   private static Set<String> internalProps = new HashSet<String>();
   static
   {
      internalProps.add(PROP_OSGI_FRAMEWORK_IMPL);
      internalProps.add(PROP_OSGI_FRAMEWORK_AUTO_INSTALL);
      internalProps.add(PROP_OSGI_FRAMEWORK_AUTO_START);
      internalProps.add(PROP_OSGI_FRAMEWORK_EXTRA);
   }

   private OSGiFramework framework;
   private boolean configured;

   public void configure()
   {
      configure(DEFAULT_OSGI_FRAMEWORK_PROPERTIES);
   }

   public void configure(URL urlConfig)
   {
      // Read the configuration properties
      Properties props = getBootstrapProperties(urlConfig);

      // Load the framework instance
      framework = loadFrameworkImpl(urlConfig, props);

      // Process Framework props
      initFrameworkProperties(props);

      // Init the the autoInstall URLs
      List<URL> installURLs = getBundleURLs(props, PROP_OSGI_FRAMEWORK_AUTO_INSTALL);
      framework.setAutoInstall(installURLs);

      // Init the the autoStart URLs
      List<URL> startURLs = getBundleURLs(props, PROP_OSGI_FRAMEWORK_AUTO_START);
      framework.setAutoStart(startURLs);

      configured = true;
   }

   private List<URL> getBundleURLs(Properties props, String key)
   {
      String bundleList = props.getProperty(key);
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

   public OSGiFramework getFramework()
   {
      if (configured == false)
      {
         String defaultFrameworkProps = System.getProperty(OSGI_FRAMEWORK_CONFIG, DEFAULT_OSGI_FRAMEWORK_PROPERTIES);
         configure(defaultFrameworkProps);
      }
      return framework;
   }

   public OSGiFramework getFramework(String name)
   {
      throw new NotImplementedException();
   }

   public Object getInstance(String name)
   {
      throw new NotImplementedException();
   }

   public <T> T getInstance(String name, Class<T> expectedType)
   {
      throw new NotImplementedException();
   }

   private void initFrameworkProperties(Properties props)
   {
      Map<String, Object> frameworkProps = new HashMap<String, Object>();
      Enumeration<?> keys = props.propertyNames();
      while (keys.hasMoreElements())
      {
         String key = (String)keys.nextElement();
         if (internalProps.contains(key) == false)
         {
            String value = props.getProperty(key);
            frameworkProps.put(key, value);
         }
      }
      framework.setProperties(frameworkProps);
   }

   private OSGiFramework loadFrameworkImpl(URL urlConfig, Properties props)
   {
      String frameworkImpl = props.getProperty(PROP_OSGI_FRAMEWORK_IMPL);
      if (frameworkImpl == null)
         throw new IllegalStateException("Cannot get : " + urlConfig);

      OSGiFramework framework;
      try
      {
         ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
         Class<?> frameworkClass = ctxLoader.loadClass(frameworkImpl);
         framework = (OSGiFramework)frameworkClass.newInstance();
      }
      catch (Exception ex)
      {
         throw new IllegalStateException("Cannot load framework: " + frameworkImpl, ex);
      }
      return framework;
   }

   @SuppressWarnings("unchecked")
   private Properties getBootstrapProperties(URL urlConfig)
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

      // Replace system properties
      Enumeration<String> keys = (Enumeration<String>)props.propertyNames();
      while (keys.hasMoreElements())
      {
         String key = keys.nextElement();
         String value = props.getProperty(key);
         props.setProperty(key, StringPropertyReplacer.replaceProperties(value));
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
               extraPropsURL = propsFile.toURL();
            }
            catch (MalformedURLException e)
            {
               // ignore;
            }
         }

         if (extraPropsURL == null)
            throw new IllegalStateException("Invalid properties URL: " + extraPropsValue);

         props.remove(PROP_OSGI_FRAMEWORK_EXTRA);
         Properties extraProps = getBootstrapProperties(extraPropsURL);
         props.putAll(extraProps);
      }

      return props;
   }

}
