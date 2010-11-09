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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.logging.Logger;

/**
 * Loads service implementations from the requesters classpath.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 14-Dec-2006
 */
public abstract class ServiceLoader
{
   // Provide logging
   private static final Logger log = Logger.getLogger(ServiceLoader.class);

   /**
    * Loads a list of service implementations defined in META-INF/services/${serviceClass}
    *
    * @param serviceClass The interface that is implemented by all loaded services
    * @return The list of available service or an empty list
    */
   @SuppressWarnings("unchecked")
   public static <T> List<T> loadServices(Class<T> serviceClass)
   {
      if (serviceClass == null)
         throw new IllegalArgumentException("Null serviceClass");

      List<T> services = new ArrayList<T>();
      ClassLoader loader = serviceClass.getClassLoader();

      // First try the system property
      String serviceClassName = System.getProperty(serviceClass.getName());
      if (serviceClassName != null)
      {
         try
         {
            Class<T> implClass = (Class<T>)loader.loadClass(serviceClassName);
            services.add(implClass.newInstance());
            return Collections.unmodifiableList(services);
         }
         catch (Exception ex)
         {
            throw new IllegalStateException("Failed to load service: " + serviceClassName, ex);
         }
      }

      // Use the Services API (as detailed in the JAR specification), if available, to determine the classname.
      String filename = "META-INF/services/" + serviceClass.getName();
      InputStream inStream = loader.getResourceAsStream(filename);
      if (inStream == null)
         log.debug("Cannot find resource: " + filename);

      if (inStream != null)
      {
         try
         {
            BufferedReader br = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
            String implClassName = br.readLine();
            while (implClassName != null)
            {
               int hashIndex = implClassName.indexOf("#");
               if (hashIndex > 0)
                  implClassName = implClassName.substring(0, hashIndex);

               implClassName = implClassName.trim();

               if (implClassName.length() > 0)
               {
                  try
                  {
                     Class<T> implClass = (Class<T>)loader.loadClass(implClassName);
                     if (serviceClass.isAssignableFrom(implClass))
                        services.add(implClass.newInstance());
                     else
                        log.warn("Not assignable: " + implClassName);
                  }
                  catch (Exception ex)
                  {
                     log.debug("Cannot load service: " + implClassName, ex);
                  }
               }

               implClassName = br.readLine();
            }
            br.close();
         }
         catch (IOException ex)
         {
            throw new IllegalStateException("Failed to load services for: " + serviceClass.getName());
         }
      }

      return Collections.unmodifiableList(services);
   }

   /**
    * Loads the first of a list of service implementations defined in META-INF/services/${serviceClass}
    *
    * @param serviceClass The interface that is implemented by all loaded services
    * @return The first available service or null
    */
   public static <T> T loadService(Class<T> serviceClass)
   {
      List<T> services = loadServices(serviceClass);
      if (services.isEmpty())
         return null;

      return services.get(0);
   }
}
