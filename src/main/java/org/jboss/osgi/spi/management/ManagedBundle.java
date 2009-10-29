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
package org.jboss.osgi.spi.management;

//$Id$

import static org.jboss.osgi.spi.OSGiConstants.DOMAIN_NAME;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.management.ObjectName;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

/**
 * The managed view of an OSGi Bundle
 * 
 * @author thomas.diesler@jboss.com
 * @since 23-Jan-2009
 */
public class ManagedBundle implements ManagedBundleMBean
{
   public static final String PROPERTY_ID = "id";
   public static final String PROPERTY_SYMBOLIC_NAME = "sname";
   public static final String PROPERTY_VERSION = "version";

   private Bundle bundle;
   private ObjectName oname;

   public ManagedBundle(Bundle bundle)
   {
      this.bundle = bundle;
      this.oname = getObjectName(bundle);
   }

   public static ObjectName getObjectName(Bundle bundle)
   {
      long id = bundle.getBundleId();
      String symbolicName = bundle.getSymbolicName();
      Version version = bundle.getVersion();
      return getObjectName(id, symbolicName, version);
   }

   public static ObjectName getObjectName(long id, String sname, Version version)
   {
      String oname = DOMAIN_NAME + ":" + PROPERTY_ID + "=" + id + "," + PROPERTY_SYMBOLIC_NAME + "=" + sname + "," + PROPERTY_VERSION + "=" + version;
      return ObjectNameFactory.create(oname);
   }

   public ObjectName getObjectName()
   {
      return oname;
   }

   public String getProperty(String key)
   {
      return bundle.getBundleContext().getProperty(key);
   }

   public int getState()
   {
      return bundle.getState();
   }

   public long getBundleId()
   {
      return bundle.getBundleId();
   }

   public String getSymbolicName()
   {
      return bundle.getSymbolicName();
   }

   @SuppressWarnings("unchecked")
   public Dictionary<String, String> getHeaders()
   {
      Hashtable<String, String> retHeaders = new Hashtable<String, String>();
      Dictionary bundleHeaders = bundle.getHeaders();
      Enumeration keys = bundleHeaders.keys();
      while (keys.hasMoreElements())
      {
         String key = (String)keys.nextElement();
         String value = (String)bundleHeaders.get(key);
         retHeaders.put(key, value);
      }
      return retHeaders;
   }

   public void start() throws BundleException
   {
      bundle.start();
   }

   public void stop() throws BundleException
   {
      bundle.stop();
   }
}