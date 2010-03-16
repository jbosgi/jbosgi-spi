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

import java.util.ArrayList;
import java.util.List;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.jboss.osgi.testing.OSGiServiceReference;
import org.osgi.jmx.JmxConstants;

/**
 * A remote implementation of the {@link OSGiServiceReference}
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 25-Sep-2008
 */
public class RemoteServiceReference implements OSGiServiceReference
{
   private TabularData propsData;
   
   public RemoteServiceReference(CompositeData serviceData, TabularData propData)
   {
      this.propsData = propData;
   }

   public Object getProperty(String key)
   {
      CompositeData propData = propsData.get(new Object[] { key });
      if (propData == null)
         return null;
      
      // [TODO] decode value
      Object value = propData.get(JmxConstants.VALUE);
      return value;
   }

   public String[] getPropertyKeys()
   {
      List<String> keys = new ArrayList<String>();
      for(Object key : propsData.keySet())
         keys.add((String)key);
      
      return keys.toArray(new String[keys.size()]);
   }
}
