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

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * A helper for various BundleContext related operations.
 * 
 * @author thomas.Diesler@jboss.org
 * @since 28-Apr-2010
 */
public class BundleContextHelper
{
   private BundleContext context;
   
   public BundleContextHelper(BundleContext context)
   {
      if (context == null)
         throw new IllegalArgumentException("Null context");
      
      this.context = context;
   }

   /**
    * Get a ServiceReference with a given timeout.
    * @return The service ref, or null if the service was not registered in time. 
    */
   public ServiceReference getServiceReference(String serviceName, int timeout)
   {
      int step = 200;
      ServiceReference sref = null;
      while (sref == null && 0 < timeout)
      {
         sref = context.getServiceReference(serviceName);
         if (sref == null)
         {
            try
            {
               Thread.sleep(step);
            }
            catch (InterruptedException ex)
            {
               // ignore
            }
            timeout -= step;
         }
      }
      return sref;
   }
}
