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

import java.util.ArrayList;
import java.util.List;

import org.jboss.osgi.spi.logging.internal.LogEntryImpl;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;

/**
 * A LogListener that caches LogEntry objects for later retrieval.
 * 
 * The entries can be filtered with a list of {@link LogEntryFilter} instances. 
 * A log entry is cached if it matches at least one of the registered filters.
 * If there is no filter registered entries are cached unconditionally.  
 * 
 * @author thomas.diesler@jboss.com
 * @since 09-Apr-2009
 */
public class LogEntryCache implements LogListener
{
   private List<LogEntry> entries = new ArrayList<LogEntry>();
   private List<LogEntryFilter> filters = new ArrayList<LogEntryFilter>();
   
   /**
    * Create a LogEntryCache with a single associated filter
    */
   public LogEntryCache(LogEntryFilter filter)
   {
      filters.add(filter);
   }

   /**
    * Create a LogEntryCache with no associated filters
    */
   public LogEntryCache()
   {
   }
   
   /**
    * Add a LogEntryFilter
    */
   public void addFilter(LogEntryFilter filter)
   {
      filters.add(filter);
   }

   /**
    * Clear the list of cached entries.
    */
   public void clear()
   {
      synchronized (entries)
      {
         entries.clear();
      }
   }

   /**
    * Clear the list of registered filters.
    */
   public void clearFilters()
   {
      // filters.clear() would need synchronization
      filters = new ArrayList<LogEntryFilter>();
   }
   
   /**
    * Get the list of cached entries.
    * 
    * Note, that the LogService delivers LogEntries asynchronously.
    * Client should not rely on a certain LogEntry already beein delivered 
    * when calling this method. 
    */
   public List<LogEntry> getLog()
   {
      return getLog(false);
   }
   
   /**
    * Get the list of cached entries and optionally clears the list.
    * 
    * Note, that the LogService delivers LogEntries asynchronously.
    * Client should not rely on a certain LogEntry already beein delivered 
    * when calling this method. 
    */
   public List<LogEntry> getLog(boolean clear)
   {
      synchronized (entries)
      {
         ArrayList<LogEntry> retList = new ArrayList<LogEntry>(entries);
         if (clear == true)
            entries.clear();
         
         return retList;
      }
   }
   
   /**
    * Listener method called for each LogEntry object created. 
    */
   public void logged(LogEntry entry)
   {
      // Replace entry with a unified wrapper
      entry = new LogEntryImpl(entry);
      
      List<LogEntryFilter> snapshot = new ArrayList<LogEntryFilter>(filters);
      synchronized (entries)
      {
         if (snapshot.size() == 0)
         {
            entries.add(entry);
            return;
         }

         // Add the entry if if matches at least one filter
         for (LogEntryFilter filter : snapshot)
         {
            if (match(filter, entry))
            {
               entries.add(entry);
               break;
            }
         }
      }
   }

   private boolean match(LogEntryFilter filter, LogEntry entry)
   {
      boolean match = entry.getLevel() <= filter.getLevel();
      
      if (match && filter.getBundleRegex() != null)
      {
         String entryBnd = entry.getBundle().getSymbolicName();
         String filterRegex = filter.getBundleRegex();
         match = entryBnd.matches(filterRegex);
      }
         
      if (match && filter.getMessageRegex() != null)
      {
         String entryMsg = entry.getMessage();
         String filterRegex = filter.getMessageRegex();
         match = entryMsg.matches(filterRegex);
      }
         
      return match;
   }
}