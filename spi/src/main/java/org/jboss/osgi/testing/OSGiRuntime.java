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

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.osgi.spi.capability.Capability;
import org.jboss.osgi.vfs.VirtualFile;
import org.jboss.shrinkwrap.api.Archive;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;
import org.osgi.jmx.framework.BundleStateMBean;
import org.osgi.jmx.framework.FrameworkMBean;
import org.osgi.jmx.framework.PackageStateMBean;
import org.osgi.jmx.framework.ServiceStateMBean;
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
    * Install an {@link OSGiBundle} from the given archive.
    */
   OSGiBundle installBundle(Archive<?> archive) throws BundleException, IOException;

   /**
    * Install an {@link OSGiBundle} from the given virtual file.
    */
   OSGiBundle installBundle(VirtualFile vfsfile) throws BundleException, IOException;

   /**
    * Install an {@link OSGiBundle} from the given location.
    */
   OSGiBundle installBundle(String location) throws BundleException;

   /**
    * Get the MBeanServerConnection for this {@link OSGiRuntime}
    */
   MBeanServerConnection getMBeanServer();

   /**
    * Get an MBeanProxy for the given interface.
    */
   <T> T getMBeanProxy(ObjectName name, Class<T> interf);
   
   /**
   * Get the FrameworkMBean
   */
   FrameworkMBean getFrameworkMBean() throws IOException;

   /**
   * Get the BundleStateMBean
   */
   BundleStateMBean getBundleStateMBean() throws IOException;

   /**
   * Get the ServiceStateMBean
   */
   ServiceStateMBean getServiceStateMBean() throws IOException;

   /**
   * Get the PackageStateMBean
   */
   PackageStateMBean getPackageStateMBean() throws IOException;

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
   OSGiBundle getBundle(String symbolicName, Version version);

   /**
    * Get the {@link OSGiBundle} for a given bundle id.
    * 
    * @return The bundle or null if there is none
    */
   OSGiBundle getBundle(long bundleId);

   /**
    * Returns a ServiceReference object for a service that implements and was registered 
    * under the specified class.
    * 
    * @return A ServiceReference object, or null  if no services are registered which implement the named class. 
    */
   OSGiServiceReference getServiceReference(String clazz);

   /**
    * Returns a ServiceReference object for a service that implements and was registered 
    * under the specified class.
    * 
    * @param timeout the timeout to wait for the service to become available
    * @return A ServiceReference object, or null  if no services are registered which implement the named class. 
    */
   OSGiServiceReference getServiceReference(String clazz, long timeout);

   /**
    * Returns an array of ServiceReference objects. 
    * The returned array of ServiceReference objects contains services that were registered under the specified 
    * class and match the specified filter criteria. 
    */
   OSGiServiceReference[] getServiceReferences(String clazz, String filter);

   /**
    * Get the initial naming context for this {@link OSGiRuntime}
    */
   InitialContext getInitialContext() throws NamingException;

   /**
    * Get the host name that this {@link OSGiRuntime} is running on.
    * 
    * This is the value of the 'jboss.bind.address' system property.
    */
   String getServerHost();

   /**
    * Return true if this {@link OSGiRuntime} connects to a remote Framework.
    */
   boolean isRemoteRuntime();

   /**
    * Refresh the packages through {@link PackageAdmin#refreshPackages(org.osgi.framework.Bundle[])} 
    */
   void refreshPackages(OSGiBundle[] bundles) throws IOException;
   
   /**
    * Shutdown the {@link OSGiRuntime}.
    * 
    * This will remove added {@link Capability}.
    */
   void shutdown();
}
