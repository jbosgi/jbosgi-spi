/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.osgi.spi.util;

// $Id: $

import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Dictionary;
import java.util.Enumeration;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

/**
 * A BundleClassLoader delegates all classloading concerns to the underlying Bundle.
 * 
 * @author Ales.Justin@jboss.org
 * @author thomas.Diesler@jboss.org
 * @since 03-Feb-2009
 */
public class BundleClassLoader extends ClassLoader
{
   private final Bundle bundle;

   public static BundleClassLoader createClassLoader(final Bundle bundle)
   {
      if (bundle == null)
         throw new IllegalArgumentException("Null bundle");

      return AccessController.doPrivileged(new PrivilegedAction<BundleClassLoader>()
      {
         public BundleClassLoader run()
         {
            return new BundleClassLoader(bundle);
         }
      });
   }

   private BundleClassLoader(Bundle bundle)
   {
      this.bundle = bundle;
   }

   protected Class<?> findClass(String name) throws ClassNotFoundException
   {
      return bundle.loadClass(name);
   }

   protected URL findResource(String name)
   {
      return bundle.getResource(name);
   }

   @SuppressWarnings("unchecked")
   protected Enumeration<URL> findResources(String name) throws IOException
   {
      return bundle.getResources(name);
   }

   public URL getResource(String name)
   {
      return findResource(name);
   }

   public Class<?> loadClass(String name) throws ClassNotFoundException
   {
      return findClass(name);
   }

   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;

      if (obj instanceof BundleClassLoader == false)
         return false;

      final BundleClassLoader bundleClassLoader = (BundleClassLoader)obj;
      return bundle.equals(bundleClassLoader.bundle);
   }

   public int hashCode()
   {
      return bundle.hashCode();
   }

   public String toString()
   {
      Dictionary<?, ?> headers = bundle.getHeaders();
      String bundleId = bundle.getSymbolicName() + ":" + headers.get(Constants.BUNDLE_VERSION);
      return "BundleClassLoader for [" + bundleId + "]";
   }
}
