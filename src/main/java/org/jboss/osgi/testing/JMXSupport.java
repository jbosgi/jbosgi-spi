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
package org.jboss.osgi.testing;

import java.io.IOException;
import java.util.ArrayList;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;

import org.jboss.logging.Logger;
import org.jboss.osgi.jmx.BundleStateMBeanExt;
import org.jboss.osgi.jmx.FrameworkMBeanExt;
import org.jboss.osgi.jmx.ObjectNameFactory;
import org.jboss.osgi.jmx.PackageStateMBeanExt;
import org.jboss.osgi.jmx.ServiceStateMBeanExt;
import org.jboss.osgi.testing.internal.ClipboardImpl;
import org.osgi.jmx.framework.BundleStateMBean;
import org.osgi.jmx.framework.FrameworkMBean;
import org.osgi.jmx.framework.PackageStateMBean;
import org.osgi.jmx.framework.ServiceStateMBean;

/**
 * An OSGi Test Helper
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 25-Sep-2008
 */
public class JMXSupport
{
   // Provide logging
   private static final Logger log = Logger.getLogger(JMXSupport.class);

   private MBeanServerConnection mbeanServer;

   public JMXSupport(MBeanServerConnection mbeanServer)
   {
      if (mbeanServer == null)
         throw new IllegalArgumentException("Null mbeanServer");
      this.mbeanServer = mbeanServer;
   }

   public static MBeanServer getLocalMBeanServer()
   {
      MBeanServer mbeanServer = null;

      ArrayList<MBeanServer> serverArr = MBeanServerFactory.findMBeanServer(null);
      if (serverArr.size() > 1)
         log.warn("Multiple MBeanServer instances: " + serverArr);

      if (serverArr.size() > 0)
      {
         mbeanServer = serverArr.get(0);
         log.debug("Found MBeanServer: " + mbeanServer);
      }

      if (mbeanServer == null)
      {
         log.debug("No MBeanServer, create one ...");
         mbeanServer = MBeanServerFactory.createMBeanServer();
         log.debug("Created MBeanServer: " + mbeanServer);
      }
      return mbeanServer;
   }

   public <T> T getProxy(ObjectName name, Class<T> interf)
   {
      return (T)MBeanServerInvocationHandler.newProxyInstance(mbeanServer, name, interf, false);
   }

   public MBeanServerConnection getMBeanServer()
   {
      return mbeanServer;
   }

   public FrameworkMBean getFrameworkMBean() throws IOException
   {
      FrameworkMBean frameworkState = null;

      ObjectName objectName = ObjectNameFactory.create(FrameworkMBeanExt.OBJECTNAME);
      if (mbeanServer.isRegistered(objectName))
      {
         frameworkState = getProxy(objectName, FrameworkMBeanExt.class);
      }
      else
      {
         objectName = ObjectNameFactory.create(FrameworkMBean.OBJECTNAME);
         frameworkState = getProxy(objectName, FrameworkMBean.class);
      }

      return frameworkState;
   }

   public BundleStateMBean getBundleStateMBean() throws IOException
   {
      BundleStateMBean bundleState = null;
      ObjectName objectName = ObjectNameFactory.create(BundleStateMBeanExt.OBJECTNAME);
      if (mbeanServer.isRegistered(objectName))
      {
         bundleState = getProxy(objectName, BundleStateMBeanExt.class);
      }
      else
      {
         objectName = ObjectNameFactory.create(BundleStateMBean.OBJECTNAME);
         bundleState = getProxy(objectName, BundleStateMBean.class);
      }
      return bundleState;
   }

   public PackageStateMBean getPackageStateMBean() throws IOException
   {
      PackageStateMBean packageState = null;
      ObjectName objectName = ObjectNameFactory.create(PackageStateMBeanExt.OBJECTNAME);
      if (mbeanServer.isRegistered(objectName))
      {
         packageState = getProxy(objectName, PackageStateMBeanExt.class);
      }
      else
      {
         objectName = ObjectNameFactory.create(PackageStateMBean.OBJECTNAME);
         packageState = getProxy(objectName, PackageStateMBean.class);
      }
      return packageState;
   }

   public ServiceStateMBean getServiceStateMBean() throws IOException
   {
      ServiceStateMBean serviceState = null;
      ObjectName objectName = ObjectNameFactory.create(ServiceStateMBeanExt.OBJECTNAME);
      if (mbeanServer.isRegistered(objectName))
      {
         serviceState = getProxy(objectName, ServiceStateMBeanExt.class);
      }
      else
      {
         objectName = ObjectNameFactory.create(ServiceStateMBean.OBJECTNAME);
         serviceState = getProxy(objectName, ServiceStateMBean.class);
      }
      return serviceState;
   }

   public ClipboardMBean getClipboardMBean() throws IOException
   {
      ObjectName objectName = ObjectNameFactory.create(ClipboardMBean.OBJECTNAME);
      if (mbeanServer.isRegistered(objectName) == false)
      {
         try
         {
            mbeanServer.createMBean(ClipboardImpl.class.getName(), objectName);
         }
         catch (Exception ex)
         {
            throw new IllegalStateException("Cannot create ClipboardMBean", ex);
         }
      }
      ClipboardMBean clipboard = getProxy(objectName, ClipboardMBean.class);
      return clipboard;
   }
}
