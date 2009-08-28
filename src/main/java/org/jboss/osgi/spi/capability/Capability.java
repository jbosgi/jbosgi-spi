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
package org.jboss.osgi.spi.capability;

//$Id$

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.osgi.spi.testing.OSGiRuntime;

/**
 * An abstract OSGi capability that can be installed in an {@link OSGiRuntime}.
 * 
 * The capability is only installed if the service name given in the constructor is not already registered with the OSGi framework.
 * 
 * It maintains an ordered set of dependent capabilities and bundles that must be installed to provide the functionality advertised by this capability.
 * 
 * @author thomas.diesler@jboss.com
 * @since 05-May-2009
 */
public abstract class Capability
{
   private String serviceName;
   private String filter;
   private Map<String, String> systemProperties;

   private List<Capability> dependencies;
   private List<String> bundles;

   /**
    * Construct a capability that is identified by the given service name. 
    * 
    * If the service name is already registered with the {@link OSGiRuntime} adding this capability
    * does nothing.
    */
   public Capability(String serviceName)
   {
      this(serviceName, null);
   }

   /**
    * Construct a capability that is identified by the given service name and filter string.
    * 
    * If the service is already registered with the {@link OSGiRuntime} adding this capability
    * does nothing.
    */
   public Capability(String serviceName, String filter)
   {
      this.serviceName = serviceName;
      this.filter = filter;
   }

   /**
    * Get the service name associated with this capability.
    */
   public String getServiceName()
   {
      return serviceName;
   }

   /**
    * Get the filter that is used for service lookup.
    */
   public String getFilter()
   {
      return filter;
   }

   /**
    * Set the filter that is used for service lookup.
    */
   public void setFilter(String filter)
   {
      this.filter = filter;
   }

   /**
    * Add a system property provided by this capability.
    * 
    * Adding this capability will set the associated system properties if a propperty is not set already.
    */
   public void addSystemProperty(String key, String value)
   {
      getPropertiesInternal().put(key, value);
   }

   /**
    * Get the system properties for this capability.
    */
   public Map<String, String> getSystemProperties()
   {
      return Collections.unmodifiableMap(getPropertiesInternal());
   }

   public List<Capability> getDependencies()
   {
      return Collections.unmodifiableList(getDependenciesInternal());
   }

   protected void addDependency(Capability dependency)
   {
      getDependenciesInternal().add(dependency);
   }

   public List<String> getBundles()
   {
      return Collections.unmodifiableList(getBundlesInternal());
   }

   protected void addBundle(String bundle)
   {
      getBundlesInternal().add(bundle);
   }

   private Map<String, String> getPropertiesInternal()
   {
      if (systemProperties == null)
         systemProperties = new HashMap<String, String>();
      
      return systemProperties;
   }

   private List<Capability> getDependenciesInternal()
   {
      if (dependencies == null)
         dependencies = new ArrayList<Capability>();

      return dependencies;
   }

   private List<String> getBundlesInternal()
   {
      if (bundles == null)
         bundles = new ArrayList<String>();

      return bundles;
   }
}