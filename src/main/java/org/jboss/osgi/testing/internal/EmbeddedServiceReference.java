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


import org.jboss.osgi.testing.OSGiServiceReference;
import org.osgi.framework.ServiceReference;

/**
 * An embedded implementation of the {@link OSGiServiceReference}
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 25-Sep-2008
 */
public class EmbeddedServiceReference implements OSGiServiceReference
{
   private ServiceReference sref;
   
   public EmbeddedServiceReference(ServiceReference sref)
   {
      this.sref = sref;
   }

   public Object getProperty(String key)
   {
      return sref.getProperty(key);
   }

   public String[] getPropertyKeys()
   {
      return sref.getPropertyKeys();
   }

}
