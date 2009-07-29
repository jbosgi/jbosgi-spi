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
package org.jboss.osgi.spi.util;

//$Id$

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;

/**
 * String representation for common OSGi Constants
 * 
 * @author thomas.diesler@jboss.com
 * @since 04-Mar-2009
 */
public abstract class OSGiConstantsHelper 
{
   /**
    * Return the string representation of a {@link Bundle} state
    */
   public static String bundleState(int bundleState)
   {
      String retState = "[" + bundleState + "]";
      if (Bundle.UNINSTALLED == bundleState)
         retState = "UNINSTALLED";
      else if (Bundle.INSTALLED == bundleState)
         retState = "INSTALLED";
      else if (Bundle.RESOLVED == bundleState)
         retState = "RESOLVED";
      else if (Bundle.STARTING == bundleState)
         retState = "STARTING";
      else if (Bundle.STOPPING == bundleState)
         retState = "STOPPING";
      else if (Bundle.ACTIVE == bundleState)
         retState = "ACTIVE";
      return retState;
   }
   
   /**
    * Return the string representation of a {@link BundleEvent} type
    */
   public static String bundleEvent(int eventType)
   {
      String retType = "[" + eventType + "]";
      if (BundleEvent.INSTALLED == eventType)
         retType = "INSTALLED";
      else if (BundleEvent.LAZY_ACTIVATION == eventType)
         retType = "LAZY_ACTIVATION";
      else if (BundleEvent.RESOLVED == eventType)
         retType = "RESOLVED";
      else if (BundleEvent.STARTING == eventType)
         retType = "STARTING";
      else if (BundleEvent.STARTED == eventType)
         retType = "STARTED";
      else if (BundleEvent.STOPPING == eventType)
         retType = "STOPPING";
      else if (BundleEvent.STOPPED == eventType)
         retType = "STOPPED";
      else if (BundleEvent.UNINSTALLED == eventType)
         retType = "UNINSTALLED";
      else if (BundleEvent.UNRESOLVED == eventType)
         retType = "UNRESOLVED";
      else if (BundleEvent.UPDATED == eventType)
         retType = "UPDATED";
      return retType;
   }
   
   /**
    * Return the string representation of a LogService level
    */
   public static String logLevel(int level)
   {
      String logLevel = "[" + level + "]";
      switch (level)
      {
         // LogService.LOG_DEBUG:
         case 0x4:
            logLevel = "DEBUG";
            break;
         // LogService.LOG_INFO:
         case 0x3:
            logLevel = "INFO";
            break;
         // LogService.LOG_WARNING:
         case 0x2:
            logLevel = "WARN";
            break;
         // LogService.LOG_ERROR
         case 0x1:
            logLevel = "ERROR";
            break;
      }
      return logLevel;
   }
}
