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

// $Id$

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.management.InstanceNotFoundException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.jboss.logging.Logger;
import org.jboss.osgi.jmx.BundleStateMBeanExt;
import org.jboss.osgi.jmx.MBeanProxy;
import org.jboss.osgi.jmx.ObjectNameFactory;
import org.jboss.osgi.spi.util.UnmodifiableDictionary;
import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiRuntime;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;
import org.osgi.jmx.JmxConstants;
import org.osgi.jmx.framework.BundleStateMBean;

/**
 * An implementation of a remote {@link OSGiBundle}
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 25-Sep-2008
 */
class RemoteBundle extends OSGiBundleImpl
{
   // Provide logging
   private static final Logger log = Logger.getLogger(RemoteBundle.class);

   private long bundleId;
   private String location;
   private String symbolicName;
   private BundleStateMBeanExt bundleState;
   private Dictionary<String, String> defaultHeaders;
   private Dictionary<String, String> rawHeaders;
   private Version version;
   boolean uninstalled;

   RemoteBundle(OSGiRuntime runtime, long bundleId) throws IOException
   {
      super(runtime);
      this.bundleId = bundleId;

      ObjectName objectName = ObjectNameFactory.create(BundleStateMBeanExt.OBJECTNAME);
      bundleState = MBeanProxy.get(runtime.getMBeanServer(), objectName, BundleStateMBeanExt.class);
      
      symbolicName = bundleState.getSymbolicName(bundleId);
      location = bundleState.getLocation(bundleId);

      String versionStr = bundleState.getVersion(bundleId);
      version = Version.parseVersion(versionStr);

      // The getHeaders methods must continue to provide the manifest header
      // information after the bundle enters the UNINSTALLED state.
      defaultHeaders = getHeadersInternal(null);
      rawHeaders = getHeadersInternal("");
   }

   @SuppressWarnings("unchecked")
   private Dictionary<String, String> getHeadersInternal(String locale) throws IOException
   {
      Dictionary<String, String> defaultHeaders = new Hashtable<String, String>();
      TabularData headers = bundleState.getHeaders(bundleId, locale);
      for (CompositeData aux : (Collection<CompositeData>)headers.values())
      {
         String key = (String)aux.get(JmxConstants.KEY);
         String value = (String)aux.get(JmxConstants.VALUE);
         defaultHeaders.put(key, value);
      }
      return new UnmodifiableDictionary(defaultHeaders);
   }

   @Override
   public int getState()
   {
      if (uninstalled == true)
         return Bundle.UNINSTALLED;

      try
      {
         BundleStateMBean bundleState = getRuntime().getBundleStateMBean();
         String state = bundleState.getState(bundleId);
         if ("INSTALLED".equals(state))
            return Bundle.INSTALLED;
         if ("RESOLVED".equals(state))
            return Bundle.RESOLVED;
         if ("STARTING".equals(state))
            return Bundle.STARTING;
         if ("ACTIVE".equals(state))
            return Bundle.ACTIVE;
         if ("STOPPING".equals(state))
            return Bundle.STOPPING;
         if ("UNINSTALLED".equals(state))
            return Bundle.UNINSTALLED;
         else
            throw new IllegalStateException("Unsupported state: " + state);
      }
      catch (Exception rte)
      {
         Throwable cause = rte.getCause() != null ? rte.getCause() : rte;
         if (cause instanceof InstanceNotFoundException == false)
            log.warn("Cannot get state for bundle: " + bundleId, cause);

         return Bundle.UNINSTALLED;
      }
   }

   @Override
   public long getBundleId()
   {
      return bundleId;
   }

   @Override
   public String getSymbolicName()
   {
      return symbolicName;
   }

   @Override
   public Version getVersion()
   {
      return version;
   }

   @Override
   public String getLocation()
   {
      return location;
   }

   @Override
   @SuppressWarnings("unchecked")
   public Dictionary<String, String> getHeaders()
   {
      return new UnmodifiableDictionary(defaultHeaders);
   }

   @Override
   public Dictionary<String, String> getHeaders(String locale)
   {
      if (locale == null)
      {
         return defaultHeaders;
      }
      else if (locale.length() == 0)
      {
         return rawHeaders;
      }
      else
      {
         try
         {
            return getHeadersInternal(locale);
         }
         catch (IOException ex)
         {
            throw new IllegalStateException("Cannot obtain headers for locale: " + locale, ex);
         }
      }
   }

   @Override
   public String getProperty(String key)
   {
      assertNotUninstalled();
      try
      {
         CompositeData propData = bundleState.getProperty(bundleId, key);
         if (propData == null)
            return null;

         return (String)propData.get(JmxConstants.VALUE);
      }
      catch (IOException ex)
      {
         throw new IllegalStateException("Cannot obtain property: " + key, ex);
      }
   }

   @Override
   public URL getEntry(String path)
   {
      assertNotUninstalled();
      try
      {
         return toURL(bundleState.getEntry(bundleId, path), null);
      }
      catch (IOException ex)
      {
         throw new IllegalStateException("Cannot getEntry: " + path, ex);
      }
   }

   @Override
   public URL getResource(String name)
   {
      assertNotUninstalled();
      try
      {
         return toURL(bundleState.getResource(bundleId, name), null);
      }
      catch (IOException ex)
      {
         throw new IllegalStateException("Cannot getResource: " + name, ex);
      }
   }

   @Override
   public File getDataFile(String filename)
   {
      assertNotUninstalled();
      try
      {
         String filepath = bundleState.getDataFile(bundleId, filename);
         return filepath != null ? new File(filepath) : null;
      }
      catch (IOException ex)
      {
         throw new IllegalStateException("Cannot getDataFile: " + filename, ex);
      }
   }

   @Override
   public OSGiBundle loadClass(String name) throws ClassNotFoundException
   {
      assertNotUninstalled();
      try
      {
         long exporterId = bundleState.loadClass(bundleId, name);
         return getRuntime().getBundle(new Long(exporterId));
      }
      catch (IOException ex)
      {
         throw new IllegalStateException("Cannot loadClass: " + name, ex);
      }
   }

   @Override
   protected void startInternal() throws BundleException
   {
      assertNotUninstalled();
      try
      {
         getRuntime().getFrameworkMBean().startBundle(bundleId);
      }
      catch (IOException ex)
      {
         throw new BundleException("Cannot start bundle: " + bundleId, ex);
      }
   }

   @Override
   protected void stopInternal() throws BundleException
   {
      assertNotUninstalled();
      try
      {
         getRuntime().getFrameworkMBean().stopBundle(bundleId);
      }
      catch (IOException ex)
      {
         throw new BundleException("Cannot stop bundle: " + bundleId, ex);
      }
   }

   @Override
   protected void uninstallInternal() throws BundleException
   {
      assertNotUninstalled();
      try
      {
         OSGiRuntimeImpl runtimeImpl = (OSGiRuntimeImpl)getRuntime();
         runtimeImpl.getFrameworkMBean().uninstallBundle(bundleId);
         runtimeImpl.unregisterBundle(this);
         uninstalled = true;
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception ex)
      {
         log.error("Cannot uninstall: " + getLocation(), ex);
      }
   }

   private URL toURL(String urlstr, URLStreamHandler sh)
   {
      if (urlstr == null)
         return null;

      try
      {
         return sh == null ? new URL(urlstr) : new URL(null, urlstr, sh);
      }
      catch (MalformedURLException ex)
      {
         // In case of the 'bundle' and 'bundleentry' protocol, use a dummy URLStreamHandler
         // Access to remote content via the bundle URL is invalid anyway
         if (sh == null && urlstr.startsWith("bundle"))
         {
            sh = new URLStreamHandler()
            {
               @Override
               protected URLConnection openConnection(URL url) throws IOException
               {
                  return null;
               }
            };
            return toURL(urlstr, sh);
         }
         throw new IllegalArgumentException("Invalid URL: " + urlstr);
      }
   }
}
