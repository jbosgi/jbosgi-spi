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
package org.jboss.osgi.spi.testing;

import javax.management.MBeanServerConnection;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.osgi.spi.capability.Capability;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * An abstraction of an OSGi Runtime.
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 25-Sep-2008
 */
public interface OSGiRuntime
{
   /**
    * Add a {@link Capability} to the runtime.
    * 
    * Adding a capability recursively adds the orderded set of dependent capabilities
    * before it installs and starts the orderded set bundles. 
    */
   void addCapability(Capability capability) throws BundleException;

   /**
    * Remove a {@link Capability} from the runtime.
    * 
    * Removing a capability does the reverse of {@link #addCapability(Capability)}.
    */
   void removeCapability(Capability capability);

   /**
    * Install an {@link OSGiBundle} from the given location.
    */
   OSGiBundle installBundle(String location) throws BundleException;
   
   /**
    * Get the array of installed {@link OSGiBundle}s
    */
   OSGiBundle[] getBundles();

   /**
    * Get the {@link OSGiBundle} for a given symbolic name and version
    * 
    * In case the version is left unspecified, it returns the first bundle that 
    * matches the symbolic name.
    * 
    * @param version may be null
    * @return The bundle or null if there is none
    */
   OSGiBundle getBundle(String symbolicName, String version);

   /**
    * Get the {@link OSGiBundle} for a given bundle id.
    * 
    * @return The bundle or null if there is none
    */
   OSGiBundle getBundle(long bundleId);

   /**
    * Get an abstraction of the {@link PackageAdmin}.
    */
   OSGiPackageAdmin getPackageAdmin();

   /**
    * Returns a ServiceReference object for a service that implements and was registered 
    * under the specified class.
    * 
    * @return A ServiceReference object, or null  if no services are registered which implement the named class. 
    */
   OSGiServiceReference getServiceReference(String clazz);
   
   /**
    * Returns an array of ServiceReference objects. 
    * The returned array of ServiceReference objects contains services that were registered under the specified 
    * class and match the specified filter criteria. 
    */
   OSGiServiceReference[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException;
   
   /**
    * Get the initial naming context for this {@link OSGiRuntime}
    */
   InitialContext getInitialContext() throws NamingException;

   /**
    * Get the MBeanServerConnection for this {@link OSGiRuntime}
    */
   MBeanServerConnection getMBeanServer();

   /**
    * Get the host name that this {@link OSGiRuntime} is running on.
    * 
    * This is the value of the 'jboss.bind.address' system property.
    */
   String getServerHost();

   /**
    * Shutdown the {@link OSGiRuntime}.
    * 
    * This will remove all installed {@link OSGiBundle}s and added {@link Capability}.
    */
   void shutdown();
}
