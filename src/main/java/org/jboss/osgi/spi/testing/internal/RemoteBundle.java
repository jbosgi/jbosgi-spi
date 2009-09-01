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

import java.util.Dictionary;

import org.jboss.logging.Logger;
import org.jboss.osgi.spi.management.ManagedBundleMBean;
import org.jboss.osgi.spi.testing.OSGiBundle;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;

/**
 * An implementation of a remote {@link OSGiBundle}
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 25-Sep-2008
 */
public class RemoteBundle extends OSGiBundle
{
   // Provide logging
   private static final Logger log = Logger.getLogger(RemoteBundle.class);
   
   private OSGiRuntimeImpl runtime;
   private ManagedBundleMBean bundle;
   private boolean uninstalled;
   private String location;

   private long bundleId;
   private String symbolicName;
   private Version version;
   
   public RemoteBundle(OSGiRuntimeImpl runtime, ManagedBundleMBean bundle, String location)
   {
      this.runtime = runtime;
      this.bundle = bundle;
      this.location = location;
      
      this.bundleId = bundle.getBundleId();
      this.symbolicName = bundle.getSymbolicName();
      
      String versionStr = getHeaders().get(Constants.BUNDLE_VERSION);
      this.version = Version.parseVersion(versionStr);
   }

   @Override
   public int getState()
   {
      return (uninstalled == true ? Bundle.UNINSTALLED : bundle.getState());
   }

   @Override
   public long getBundleId()
   {
      return bundleId;
   }

   @Override
   public String getSymbolicName()
   {
      return symbolicName;
   }

   @Override
   public Version getVersion()
   {
      return version;
   }

   @Override
   public Dictionary<String, String> getHeaders()
   {
      assertNotUninstalled();
      return bundle.getHeaders();
   }

   @Override
   public String getProperty(String key)
   {
      assertNotUninstalled();
      return bundle.getProperty(key);
   }

   @Override
   public void start() throws BundleException
   {
      assertNotUninstalled();
      bundle.start();
   }

   @Override
   public void stop() throws BundleException
   {
      assertNotUninstalled();
      bundle.stop();
   }

   @Override
   public void uninstall() throws BundleException
   {
      assertNotUninstalled();
      try
      {
         runtime.undeploy(location);
         runtime.unregisterBundle(this);
         uninstalled = true;
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception ex)
      {
         log.error("Cannot uninstall: " + location);
      }
   }

   private void assertNotUninstalled()
   {
      if (uninstalled == true)
         throw new IllegalStateException("Bundle already uninstalled: " + location);
   }
}
