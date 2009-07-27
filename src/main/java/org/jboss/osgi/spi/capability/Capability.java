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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.jboss.osgi.spi.testing.OSGiRuntime;

/**
 * An abstract OSGi capability that can be installed in an 
 * {@link OSGiRuntime}.
 * 
 * The capability is only installed if the service name given in the constructor
 * is not already registered with the OSGi framework. 
 * 
 * It maintains an ordered set of dependent capabilities and bundles that 
 * must be installed to provide the functionality advertised by this capability. 
 * 
 * @author thomas.diesler@jboss.com
 * @since 05-May-2009
 */
public abstract class Capability
{
   private String serviceName;
   private Properties props = new Properties();
   
   private Set<Capability> dependencies = new LinkedHashSet<Capability>();
   private Set<String> bundles = new LinkedHashSet<String>();

   /**
    * Construct a capability that is identified by the given service name.
    * If the service name is already registered with the {@link OSGiRuntime}
    * adding this capability does nothing. 
    */
   public Capability(String serviceName)
   {
      this.serviceName = serviceName;
   }

   /**
    * Get the service name associated with this capability.
    */
   public String getServiceName()
   {
      return serviceName;
   }

   /**
    * Get system properties provided by this capability.
    * 
    * Adding this capability will set the associated system properties
    * if a propperty is not set already.
    */
   public Properties getProperties()
   {
      return props;
   }

   public List<Capability> getDependencies()
   {
      return new ArrayList<Capability>(dependencies);
   }
   
   public List<String> getBundles()
   {
      return new ArrayList<String>(bundles);
   }

   protected void addBundle(String bundle)
   {
      bundles.add(bundle);
   }

   protected void addDependency(Capability dependency)
   {
      dependencies.add(dependency);
   }
}