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
import java.io.InputStream;
import java.util.Dictionary;

import org.jboss.logging.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * A generic BundleContext wrapper that delegates all method calls to the underlying 
 * BundleContext implementation.
 * 
 * @author thomas.diesler@jboss.com
 * @since 16-Oct-2009
 */
public class BundleContextWrapper implements BundleContext
{
   // Provide logging
   final Logger log = Logger.getLogger(BundleContextWrapper.class);

   protected BundleContext context;

   public BundleContextWrapper(BundleContext context)
   {
      if (context == null)
         throw new IllegalArgumentException("Null framework");
      this.context = context;
   }
   
   public void addBundleListener(BundleListener listener)
   {
      context.addBundleListener(listener);
   }

   public void addFrameworkListener(FrameworkListener listener)
   {
      context.addFrameworkListener(listener);
   }

   public void addServiceListener(ServiceListener listener, String filter) throws InvalidSyntaxException
   {
      context.addServiceListener(listener, filter);
   }

   public void addServiceListener(ServiceListener listener)
   {
      context.addServiceListener(listener);
   }

   public Filter createFilter(String filter) throws InvalidSyntaxException
   {
      return context.createFilter(filter);
   }

   public ServiceReference[] getAllServiceReferences(String clazz, String filter) throws InvalidSyntaxException
   {
      return context.getAllServiceReferences(clazz, filter);
   }

   public Bundle getBundle()
   {
      return context.getBundle();
   }

   public Bundle getBundle(long id)
   {
      return context.getBundle(id);
   }

   public Bundle[] getBundles()
   {
      return context.getBundles();
   }

   public File getDataFile(String filename)
   {
      return context.getDataFile(filename);
   }

   public String getProperty(String key)
   {
      return context.getProperty(key);
   }

   public Object getService(ServiceReference reference)
   {
      return context.getService(reference);
   }

   public ServiceReference getServiceReference(String clazz)
   {
      return context.getServiceReference(clazz);
   }

   public ServiceReference[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException
   {
      return context.getServiceReferences(clazz, filter);
   }

   public Bundle installBundle(String location, InputStream input) throws BundleException
   {
      return context.installBundle(location, input);
   }

   public Bundle installBundle(String location) throws BundleException
   {
      return context.installBundle(location);
   }

   @SuppressWarnings("unchecked")
   public ServiceRegistration registerService(String clazz, Object service, Dictionary properties)
   {
      return context.registerService(clazz, service, properties);
   }

   @SuppressWarnings("unchecked")
   public ServiceRegistration registerService(String[] clazzes, Object service, Dictionary properties)
   {
      return context.registerService(clazzes, service, properties);
   }

   public void removeBundleListener(BundleListener listener)
   {
      context.removeBundleListener(listener);
   }

   public void removeFrameworkListener(FrameworkListener listener)
   {
      context.removeFrameworkListener(listener);
   }

   public void removeServiceListener(ServiceListener listener)
   {
      context.removeServiceListener(listener);
   }

   public boolean ungetService(ServiceReference reference)
   {
      return context.ungetService(reference);
   }
}