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
package org.jboss.osgi.spi.testing.internal;

import org.jboss.osgi.spi.management.MBeanProxy;
import org.jboss.osgi.spi.management.MBeanProxyException;
import org.jboss.osgi.spi.management.ManagedFrameworkMBean;
import org.jboss.osgi.spi.testing.OSGiBundle;
import org.jboss.osgi.spi.testing.OSGiPackageAdmin;

/**
 * A remote implementation of the {@link OSGiPackageAdmin}
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 25-Sep-2008
 */
public class RemotePackageAdmin implements OSGiPackageAdmin
{
   private OSGiRuntimeImpl runtime;

   public RemotePackageAdmin(OSGiRuntimeImpl runtime)
   {
      this.runtime = runtime;
   }

   public void refreshPackages(OSGiBundle[] bundles)
   {
      String[] bundleArr = null;
      if (bundles != null)
      {
         bundleArr = new String[bundles.length];
         for (int i=0; i < bundles.length; i++)
         {
            bundleArr[i] = bundles[i].getSymbolicName();
         }
      }
      try
      {
         ManagedFrameworkMBean mbeanProxy = MBeanProxy.get(ManagedFrameworkMBean.class, ManagedFrameworkMBean.MBEAN_MANAGED_FRAMEWORK, runtime.getMBeanServer());
         mbeanProxy.refreshPackages(bundleArr);
      }
      catch (MBeanProxyException ex)
      {
         throw new IllegalStateException("Cannot refresh packages", ex);
      }
   }
}
