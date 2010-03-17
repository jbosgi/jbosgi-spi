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

import org.jboss.logging.Logger;
import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiRuntime;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

/**
 * An abstract implementation of a {@link OSGiBundle}
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 25-Sep-2008
 */
public abstract class OSGiBundleImpl implements OSGiBundle
{
   // Provide logging
   private static final Logger log = Logger.getLogger(OSGiBundleImpl.class);

   private OSGiRuntime runtime;
   
   OSGiBundleImpl(OSGiRuntime runtime)
   {
      this.runtime = runtime;
   }

   public OSGiRuntime getRuntime()
   {
      return runtime;
   }

   /**
    * Starts this bundle.
    */
   public void start() throws BundleException
   {
      log.debug("Start bundle: " + this);
      startInternal();
   }
   
   protected abstract void startInternal() throws BundleException;
   
   /**
    * Stops this bundle.
    */
   public void stop() throws BundleException
   {
      log.debug("Stop bundle: " + this);
      stopInternal();
   }
   
   protected abstract void stopInternal() throws BundleException;
   
   /**
    * Uninstalls this bundle.
    */
   public void uninstall() throws BundleException
   {
      log.debug("Uninstall bundle: " + this);
      uninstallInternal();
   }
   
   protected abstract void uninstallInternal() throws BundleException;
   
   /**
    * Return true if symbolic name and version are equal
    */
   public boolean equals(Object obj)
   {
      if ((obj instanceof OSGiBundle) == false)
         return false;
      
      OSGiBundle other = (OSGiBundle)obj;
      
      boolean isEqual =  getSymbolicName().equals(other.getSymbolicName());
      isEqual = isEqual && getVersion().equals(other.getVersion());
      return isEqual;
   }

   /**
    * Returns the hash code for this bundle. 
    */
   public int hashCode()
   {
      return toString().hashCode();
   }

   /**
    * Returns the string representation of this bundle 
    */
   public String toString()
   {
      return "[" + getSymbolicName() + ":" + getVersion() + "]";
   }
   
   void assertNotUninstalled()
   {
      if (getState() == Bundle.UNINSTALLED)
         throw new IllegalStateException("Bundle already uninstalled: " + getLocation());
   }
}
