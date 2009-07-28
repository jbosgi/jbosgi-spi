/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Loads a service from the requesters classpath.
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 14-Dec-2006
 */
public abstract class ServiceLoader
{
   /**
    * Loads the requested service from META-INF/services/${serviceClass}
    */
   @SuppressWarnings("unchecked")
   public static <T> T loadService(Class<T> serviceClass)
   {
      T factory = null;
      String factoryName = null;
      ClassLoader loader = serviceClass.getClassLoader();

      // Use the Services API (as detailed in the JAR specification), if available, to determine the classname.
      String filename = "META-INF/services/" + serviceClass.getName();
      InputStream inStream = loader.getResourceAsStream(filename);
      if (inStream != null)
      {
         try
         {
            BufferedReader br = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
            factoryName = br.readLine();
            br.close();
            if (factoryName != null)
            {
               factoryName = factoryName.trim();
               Class<T> factoryClass = (Class<T>)loader.loadClass(factoryName);
               factory = factoryClass.newInstance();
            }
         }
         catch (Throwable t)
         {
            throw new IllegalStateException("Failed to load " + serviceClass.getName() + ": " + factoryName, t);
         }
      }
      
      return factory;
   }
}
