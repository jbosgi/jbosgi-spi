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

import java.net.URL;

import javax.management.MBeanException;
import javax.management.ObjectName;

import org.jboss.osgi.jmx.ObjectNameFactory;
import org.jboss.osgi.testing.OSGiRuntime;
import org.osgi.framework.BundleException;

/**
 * An abstract implementation of the {@link OSGiRuntime}
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 25-Sep-2008
 */
class MainDeployerClient 
{
   private final OSGiRuntime runtime;

   MainDeployerClient(OSGiRuntime runtime)
   {
      this.runtime = runtime;
   }

   public void deploy(URL url) throws BundleException
   {
      invokeDeployerMBean("deploy", url);
   }

   public void undeploy(URL url) throws BundleException
   {
      invokeDeployerMBean("undeploy", url);
   }

   private void invokeDeployerMBean(String method, URL url) throws BundleException
   {
      try
      {
         ObjectName objectName = ObjectNameFactory.create("jboss.system:service=MainDeployer");
         runtime.getMBeanServer().invoke(objectName, method, new Object[] { url }, new String[] { URL.class.getName() });
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception ex)
      {
         if (ex instanceof MBeanException)
         {
            ex = ((MBeanException)ex).getTargetException();
            if (ex instanceof BundleException)
               throw (BundleException)ex;
         }
         
         throw new BundleException("Cannot " + method + ": " + url, ex);
      }
   }
}