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
package org.jboss.osgi.spi.management;

//$Id$

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.QueryExp;

import org.jboss.logging.Logger;
import org.jboss.osgi.spi.Constants;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * The managed view of an OSGi Framework
 * 
 * @author thomas.diesler@jboss.com
 * @since 04-Mar-2009
 */
public class ManagedFramework implements ManagedFrameworkMBean
{
   // Provide logging
   final Logger log = Logger.getLogger(ManagedFramework.class);

   private MBeanServer mbeanServer;
   private BundleContext bundleContext;

   public ManagedFramework(BundleContext bundleContext, MBeanServer mbeanServer)
   {
      if (bundleContext == null)
         throw new IllegalArgumentException("Null BundleContext");
      this.bundleContext = bundleContext;
      
      if (mbeanServer == null)
         throw new IllegalArgumentException("Null MBeanServer");
      this.mbeanServer = mbeanServer;
      
      if (bundleContext.getBundle().getBundleId() != 0)
         throw new IllegalArgumentException ("Not the system bundle context: " + bundleContext);
   }

   public BundleContext getBundleContext()
   {
      return bundleContext;
   }

   @SuppressWarnings("unchecked")
   public ObjectName getBundle(String symbolicName, String version)
   {
      ObjectName oname = null;

      ObjectName pattern = ObjectNameFactory.create(Constants.DOMAIN_NAME + ":bundle=" + symbolicName + ",*");
      Set<ObjectName> names = mbeanServer.queryNames(pattern, null);

      if (names.size() > 0)
      {
         // [TODO] Support bundle version 
         if (names.size() > 1)
            throw new IllegalArgumentException("Multiple bundles found: " + names);

         oname = names.iterator().next();
      }

      return oname;
   }

   @SuppressWarnings("unchecked")
   public ObjectName getBundle(long bundleId)
   {
      ObjectName oname = null;

      ObjectName pattern = ObjectNameFactory.create(Constants.DOMAIN_NAME + ":id=" + bundleId + ",*");
      Set<ObjectName> names = mbeanServer.queryNames(pattern, null);

      if (names.size() > 0)
         oname = names.iterator().next();

      return oname;
   }

   @SuppressWarnings("unchecked")
   public Set<ObjectName> getBundles()
   {
      // [JBAS-6571] JMX filtering does not work with wildcards
      // ObjectName pattern = ObjectNameFactory.create(Constants.DOMAIN_NAME + ":bundle=*,*");
      // Set<ObjectName> names = mbeanServer.queryNames(pattern, null);

      ObjectName pattern = ObjectNameFactory.create(Constants.DOMAIN_NAME + ":*");
      Set<ObjectName> names = mbeanServer.queryNames(pattern, new IsBundleQueryExp());
      return names;
   }

   public ManagedServiceReference getServiceReference(String clazz)
   {
      ServiceReference sref = getBundleContext().getServiceReference(clazz);
      if (sref == null)
         return null;

      Map<String, Object> props = new HashMap<String, Object>();
      for (String key : sref.getPropertyKeys())
      {
         props.put(key, sref.getProperty(key));
      }
      
      return new ManagedServiceReference(props);
   }

   public ManagedServiceReference[] getServiceReferences(String clazz, String filter)
   {
      List<ManagedServiceReference> foundRefs = new ArrayList<ManagedServiceReference>();
      
      ServiceReference[] srefs;
      try
      {
         srefs = getBundleContext().getServiceReferences(clazz, filter);
      }
      catch (InvalidSyntaxException e)
      {
         throw new IllegalArgumentException("Invalid filter syntax: " + filter);
      }
      
      if (srefs != null)
      {
         for (ServiceReference sref : srefs)
         {
            Map<String, Object> props = new HashMap<String, Object>();
            for (String key : sref.getPropertyKeys())
               props.put(key, sref.getProperty(key));

            foundRefs.add(new ManagedServiceReference(props));
         }
      }

      ManagedServiceReference[] manrefs = null;
      if (foundRefs.size() > 0)
         manrefs = foundRefs.toArray(new ManagedServiceReference[foundRefs.size()]);

      return manrefs;
   }

   public void refreshPackages(String[] symbolicNames)
   {
      ServiceReference sref = getBundleContext().getServiceReference(PackageAdmin.class.getName());
      if (sref != null)
      {
         PackageAdmin service = (PackageAdmin)getBundleContext().getService(sref);

         Bundle[] bundles = null;
         if (symbolicNames != null)
         {
            List<String> nameList = Arrays.asList(symbolicNames);
            Set<Bundle> bundleSet = new HashSet<Bundle>();
            for (Bundle bundle : getBundleContext().getBundles())
            {
               if (nameList.contains(bundle.getSymbolicName()))
                  bundleSet.add(bundle);
            }
            bundles = new Bundle[bundleSet.size()];
            bundleSet.toArray(bundles);
         }
         service.refreshPackages(bundles);
      }
   }

   public void start()
   {
      try
      {
         if (mbeanServer != null)
            mbeanServer.registerMBean(this, ManagedFrameworkMBean.MBEAN_MANAGED_FRAMEWORK);
      }
      catch (JMException ex)
      {
         log.warn("Cannot register: " + ManagedFrameworkMBean.MBEAN_MANAGED_FRAMEWORK);
      }
   }

   public void stop()
   {
      try
      {
         if (mbeanServer != null && mbeanServer.isRegistered(MBEAN_MANAGED_FRAMEWORK))
            mbeanServer.unregisterMBean(ManagedFrameworkMBean.MBEAN_MANAGED_FRAMEWORK);
      }
      catch (JMException ex)
      {
         log.warn("Cannot register: " + ManagedFrameworkMBean.MBEAN_MANAGED_FRAMEWORK);
      }
   }

   // Accept names like "jboss.osgi:bundle=*"
   static class IsBundleQueryExp implements QueryExp
   {
      private static final long serialVersionUID = 1L;

      public boolean apply(ObjectName name)
      {
         return name.getKeyProperty("bundle") != null;
      }

      public void setMBeanServer(MBeanServer server)
      {
      }
   }
}