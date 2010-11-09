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

import org.jboss.logging.Logger;
import org.jboss.osgi.spi.framework.OSGiBootstrap;
import org.jboss.osgi.spi.framework.OSGiBootstrapProvider;
import org.osgi.framework.Bundle;

/**
 * A helper for the OSGi runtime abstraction. 
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 25-Sep-2008
 */
public class OSGiRuntimeHelper extends OSGiTestHelper
{
   // Provide logging
   private static final Logger log = Logger.getLogger(OSGiRuntimeHelper.class);
   
   // The OSGiBootstrapProvider is a lazy property of the helper
   private OSGiBootstrapProvider bootProvider;
   private boolean skipBootstrap;

   public OSGiBootstrapProvider getBootstrapProvider()
   {
      if (bootProvider == null && skipBootstrap == false)
      {
         try
         {
            bootProvider = OSGiBootstrap.getBootstrapProvider();
         }
         catch (RuntimeException rte)
         {
            skipBootstrap = true;
            throw rte;
         }
      }
      return bootProvider;
   }

   public void ungetBootstrapProvider()
   {
      bootProvider = null;
   }

   public static void failsafeStop(OSGiBundle bundle)
   {
      if (bundle != null)
      {
         try
         {
            bundle.stop();
         }
         catch (Exception ex)
         {
            log.warn("Cannot stop bundle: " + bundle, ex);
         }
      }
   }
   
   public static void failsafeUninstall(OSGiBundle bundle)
   {
      if (bundle != null)
      {
         try
         {
            if (bundle.getState() != Bundle.UNINSTALLED)
               bundle.uninstall();
         }
         catch (Exception ex)
         {
            log.warn("Cannot uninstall bundle: " + bundle, ex);
         }
      }
   }
}
