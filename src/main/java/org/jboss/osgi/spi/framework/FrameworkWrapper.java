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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.framework.launch.Framework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A generic Framework wrapper that delegates all method calls to the underlying 
 * Framework implementation.
 * 
 * @author thomas.diesler@jboss.com
 * @since 16-Oct-2009
 */
public class FrameworkWrapper implements Framework
{
   // Provide logging
   final Logger log = LoggerFactory.getLogger(FrameworkWrapper.class);

   protected Framework framework;

   public FrameworkWrapper(Framework framework)
   {
      if (framework == null)
         throw new IllegalArgumentException("Null framework");
      this.framework = framework;
   }

   @SuppressWarnings("unchecked")
   public Enumeration findEntries(String arg0, String arg1, boolean arg2)
   {
      return framework.findEntries(arg0, arg1, arg2);
   }

   public BundleContext getBundleContext()
   {
      return framework.getBundleContext();
   }

   public long getBundleId()
   {
      return framework.getBundleId();
   }

   public URL getEntry(String arg0)
   {
      return framework.getEntry(arg0);
   }

   @SuppressWarnings("unchecked")
   public Enumeration getEntryPaths(String arg0)
   {
      return framework.getEntryPaths(arg0);
   }

   @SuppressWarnings("unchecked")
   public Dictionary getHeaders()
   {
      return framework.getHeaders();
   }

   @SuppressWarnings("unchecked")
   public Dictionary getHeaders(String arg0)
   {
      return framework.getHeaders(arg0);
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

   public URL getResource(String arg0)
   {
      return framework.getResource(arg0);
   }

   @SuppressWarnings("unchecked")
   public Enumeration getResources(String arg0) throws IOException
   {
      return framework.getResources(arg0);
   }

   public ServiceReference[] getServicesInUse()
   {
      return framework.getServicesInUse();
   }

   @SuppressWarnings("unchecked")
   public Map getSignerCertificates(int arg0)
   {
      return framework.getSignerCertificates(arg0);
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

   public boolean hasPermission(Object arg0)
   {
      return framework.hasPermission(arg0);
   }

   public void init() throws BundleException
   {
      framework.init();
   }

   @SuppressWarnings("unchecked")
   public Class loadClass(String arg0) throws ClassNotFoundException
   {
      return framework.loadClass(arg0);
   }

   public void start() throws BundleException
   {
      framework.start();
   }

   public void start(int arg0) throws BundleException
   {
      framework.start(arg0);
   }

   public void stop() throws BundleException
   {
      framework.stop();
   }

   public void stop(int arg0) throws BundleException
   {
      framework.stop(arg0);
   }

   public void uninstall() throws BundleException
   {
      framework.uninstall();
   }

   public void update() throws BundleException
   {
      framework.update();
   }

   public void update(InputStream arg0) throws BundleException
   {
      framework.update(arg0);
   }

   public FrameworkEvent waitForStop(long arg0) throws InterruptedException
   {
      return framework.waitForStop(arg0);
   }
}