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
package org.jboss.osgi.spi.logging;

//$Id$


/**
 * A LogEntry filter that can be used with the LogEntryCache  
 * 
 * @author thomas.diesler@jboss.com
 * @since 09-Apr-2009
 */
public class LogEntryFilter
{
   private int level;
   private String bndRegex;
   private String msgRegex;
   
   /**
    * Create a LogEntryFilter with the associated criteria.
    *  
    * @param bndRegex A regex that matches a Bundle's SymbolicName
    * @param level The maximum log level accepted by this filter
    * @param msgRegex A regex that matches the log message
    */
   public LogEntryFilter(String bndRegex, int level, String msgRegex)
   {
      this.bndRegex = bndRegex;
      this.msgRegex = msgRegex;
      this.level = level < 1 ? Integer.MAX_VALUE : level;
   }
   
   /**
    * Create a LogEntryFilter with the associated criteria.
    *  
    * @param bndRegex A regex that matches a Bundle's SymbolicName
    * @param level The maximum log level accepted by this filter
    */
   public LogEntryFilter(String bndRegex, int level)
   {
      this(bndRegex, level, null);
   }
   
   /**
    * Create a LogEntryFilter with the associated criteria.
    *  
    * @param bndRegex A regex that matches a Bundle's SymbolicName
    */
   public LogEntryFilter(String bndRegex)
   {
      this(bndRegex, Integer.MAX_VALUE, null);
   }
   
   /**
    * Get the Bundle Symbolic-Name regex.
    */
   public String getBundleRegex()
   {
      return bndRegex;
   }

   /**
    * Get the log message regex.
    */
   public String getMessageRegex()
   {
      return msgRegex;
   }

   /**
    * Get the log entry maximum log level.
    */
   public int getLevel()
   {
      return level;
   }
}