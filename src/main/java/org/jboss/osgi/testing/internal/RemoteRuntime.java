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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.logging.Logger;
import org.jboss.osgi.jmx.JMXConstantsExt;
import org.jboss.osgi.jmx.MBeanProxy;
import org.jboss.osgi.jmx.ObjectNameFactory;
import org.jboss.osgi.jmx.ServiceStateMBeanExt;
import org.jboss.osgi.spi.util.BundleInfo;
import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiRuntime;
import org.jboss.osgi.testing.OSGiRuntimeHelper;
import org.jboss.osgi.testing.OSGiServiceReference;
import org.osgi.framework.BundleException;
import org.osgi.jmx.framework.BundleStateMBean;
import org.osgi.jmx.framework.ServiceStateMBean;

/**
 * A remote implementation of the {@link OSGiRuntime}
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 25-Sep-2008
 */
public class RemoteRuntime extends OSGiRuntimeImpl
{
   // Provide logging
   private static final Logger log = Logger.getLogger(RemoteRuntime.class);

   public RemoteRuntime(OSGiRuntimeHelper helper)
   {
      super(helper);
   }

   @Override
   OSGiBundle installBundleInternal(BundleInfo info) throws BundleException
   {
      try
      {
         String location = info.getLocation();
         String streamURL = info.getRoot().getStreamURL().toExternalForm();
         long bundleId = getFrameworkMBean().installBundleFromURL(location, streamURL);
         return new RemoteBundle(this, bundleId);
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception ex)
      {
         throw new BundleException("Cannot install: " + info, ex);
      }
   }

   public void deploy(String location) throws Exception
   {
      URL archiveURL = getTestHelper().getTestArchiveURL(location);
      MainDeployerClient deployer = new MainDeployerClient(this);
      deployer.deploy(archiveURL);
   }

   public void undeploy(String location) throws Exception
   {
      URL archiveURL = getTestHelper().getTestArchiveURL(location);
      MainDeployerClient deployer = new MainDeployerClient(this);
      deployer.undeploy(archiveURL);
   }

   @Override
   public OSGiBundle[] getBundles()
   {
      Set<OSGiBundle> bundles = new HashSet<OSGiBundle>();
      try
      {
         TabularData listBundles = getBundleStateMBean().listBundles();
         Iterator<?> iterator = listBundles.values().iterator();
         while (iterator.hasNext())
         {
            CompositeData bundleType = (CompositeData)iterator.next();
            Long bundleId = (Long)bundleType.get(BundleStateMBean.IDENTIFIER);
            try
            {
               bundles.add(new RemoteBundle(this, bundleId));
            }
            catch (IOException ex)
            {
               log.warn("Cannot initialize remote bundle: [" + bundleId + "] - " + ex.getMessage());
            }
         }
         OSGiBundle[] bundleArr = new OSGiBundle[bundles.size()];
         bundles.toArray(bundleArr);
         return bundleArr;
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception ex)
      {
         throw new IllegalStateException("Cannot obtain remote bundles", ex);
      }
   }

   @Override
   public OSGiBundle getBundle(long bundleId)
   {
      for (OSGiBundle bundle : getBundles())
      {
         if (bundleId == bundle.getBundleId())
            return bundle;
      }
      return null;
   }

   @Override
   public OSGiServiceReference getServiceReference(String clazz)
   {
      CompositeData serviceData;
      TabularData propertiesData;
      try
      {
         ServiceStateMBeanExt serviceState = getServiceStateMBeanExt();
         serviceData = serviceState.getService(clazz);
         if (serviceData == null)
            return null;

         Long serviceId = (Long)serviceData.get(ServiceStateMBean.IDENTIFIER);
         propertiesData = serviceState.getProperties(serviceId);
      }
      catch (IOException ex)
      {
         throw new IllegalStateException(ex);
      }
      return new RemoteServiceReference(serviceData, propertiesData);
   }

   @Override
   @SuppressWarnings("unchecked")
   public OSGiServiceReference[] getServiceReferences(String clazz, String filter)
   {
      TabularData servicesData;
      List<OSGiServiceReference> srefs;
      try
      {
         ServiceStateMBeanExt serviceState = getServiceStateMBeanExt();
         servicesData = serviceState.getServices(clazz, filter);
         if (servicesData == null)
            return null;

         srefs = new ArrayList<OSGiServiceReference>();
         for (CompositeData serviceData : (Collection<CompositeData>)servicesData.values())
         {
            Long serviceId = (Long)serviceData.get(ServiceStateMBean.IDENTIFIER);
            TabularData propertiesData = serviceState.getProperties(serviceId);
            srefs.add(new RemoteServiceReference(serviceData, propertiesData));
         }
      }
      catch (IOException ex)
      {
         throw new IllegalStateException(ex);
      }
      return srefs.toArray(new OSGiServiceReference[servicesData.size()]);
   }

   private ServiceStateMBeanExt getServiceStateMBeanExt()
   {
      ObjectName objectName = ObjectNameFactory.create(ServiceStateMBeanExt.OBJECTNAME);
      return MBeanProxy.get(getMBeanServer(), objectName, ServiceStateMBeanExt.class);
   }

   @Override
   public MBeanServerConnection getMBeanServer()
   {
      MBeanServerConnection mbeanServer = null;
      try
      {
         String host = getServerHost();
         String rmiPort = System.getProperty(JMXConstantsExt.REMOTE_JMX_RMI_PORT, "1098");

         // Construct the JSR160 remote address  
         JMXServiceURL address = new JMXServiceURL("service:jmx:rmi://" + host + "/jndi/rmi://" + host + ":" + rmiPort + "/jmxconnector");

         // The environment map, null in this case
         Map<String, ?> env = null;

         // Create the JMXCconnectorServer
         JMXConnector cntor = JMXConnectorFactory.connect(address, env);

         // Obtain a "stub" for the remote MBeanServer
         mbeanServer = cntor.getMBeanServerConnection();
      }
      catch (Exception ex)
      {
         log.warn("Cannot obtain MBeanServerConnection", ex);
      }

      // Fall back to the legacy JNDI name
      if (mbeanServer == null)
      {
         try
         {
            InitialContext iniCtx = getInitialContext();
            mbeanServer = (MBeanServerConnection)iniCtx.lookup("jmx/invoker/RMIAdaptor");
         }
         catch (NamingException ex)
         {
            log.warn("Cannot obtain MBeanServerConnection", ex);
         }
      }
      
      return mbeanServer;
   }

   @Override
   public boolean isRemoteRuntime()
   {
      return true;
   }
}
