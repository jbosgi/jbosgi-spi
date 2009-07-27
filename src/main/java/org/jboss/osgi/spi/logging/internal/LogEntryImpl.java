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
package org.jboss.osgi.spi.logging.internal;

// $Id$

import java.text.SimpleDateFormat;
import java.util.Date;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogService;

/**
 * A unified implementation of a LogEntry.
 * 
 * @author thomas.diesler@jboss.com
 * @since 09-Apr-2009
 */
public class LogEntryImpl implements LogEntry
{
   private long time;
   private int level;
   private Bundle bundle;
   private ServiceReference sref;
   private String message;
   private Throwable exception;
   
   private String bndStr;
   private String srefStr;
   
   public LogEntryImpl(LogEntry le)
   {
      this(le.getTime(), le.getBundle(), le.getServiceReference(), le.getLevel(), le.getMessage(), le.getException());
   }
   
   public LogEntryImpl(long time, Bundle bundle, ServiceReference sref, int level, String message, Throwable exception)
   {
      this.time = time;
      this.bundle = bundle;
      this.sref = sref;
      this.level = level;
      this.message = message;
      this.exception = exception;
      
      if (bundle != null)
         bndStr = bundle.getSymbolicName();
      
      if (sref != null && sref.getBundle() != null)
         srefStr = sref.getBundle().getSymbolicName();
   }

   public Bundle getBundle()
   {
      return bundle;
   }

   public Throwable getException()
   {
      return exception;
   }

   public int getLevel()
   {
      return level;
   }

   public String getMessage()
   {
      return message;
   }

   public ServiceReference getServiceReference()
   {
      return sref;
   }

   public long getTime()
   {
      return time;
   }

   private String logLevel(int level)
   {
      String logLevel;
      switch (level)
      {
         case LogService.LOG_DEBUG:
            logLevel = "DEBUG";
            break;
         case LogService.LOG_INFO:
            logLevel = "INFO";
            break;
         case LogService.LOG_WARNING:
            logLevel = "WARN";
            break;
         case LogService.LOG_ERROR:
            logLevel = "ERROR";
            break;
         default:
            logLevel = "Level=" + level;
      }
      return logLevel;
   }
   
   @Override
   public String toString()
   {
      String t = new SimpleDateFormat("dd-MMM-yyyy HH:mm.ss.SSS").format(new Date(time));
      String l = " " + logLevel(level);
      String s = srefStr != null ? ",sref=" + srefStr : "";
      String b = ",bnd=" + bndStr;
      String m = ",msg=" + message;
      String e = exception != null ? ",ex=" + exception : "";
      return "[" + t + l + b + s + m + e + "]";
   }
}