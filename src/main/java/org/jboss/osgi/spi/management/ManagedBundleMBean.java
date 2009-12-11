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

import java.util.Dictionary;

import javax.management.ObjectName;

import org.osgi.framework.BundleException;

/**
 * The managed view of an OSGi Bundle.
 * 
 * Bundles are registered under the name
 * 
 * jboss.osgi:bundle=[SymbolicName],id=[BundleId]
 * 
 * @author thomas.diesler@jboss.com
 * @since 04-Mar-2009
 */
public interface ManagedBundleMBean
{
   /**
    * Get the bundles object name.
    */
   ObjectName getObjectName();

   /**
    * Returns this bundle's current state. A bundle can be in only one state at any time.
    * 
    * @return An element of UNINSTALLED,INSTALLED, RESOLVED,STARTING, STOPPING,ACTIVE.
    */
   int getState();

   /**
    * Returns this bundle's unique identifier.
    */
   long getBundleId();

   /**
    * Returns the symbolic name of this bundle as specified by its Bundle-SymbolicName manifest header
    */
   String getSymbolicName();

   /**
    * Returns the location of this bundle
    */
   String getLocation();

   /**
    * Returns the bundle manifest headers
    */
   Dictionary<String, String> getHeaders();

   /**
    * Returns the value of the specified property from the BundleContext.
    */
   String getProperty(String key);

   /**
    * Starts this bundle with no options
    */
   void start() throws BundleException;

   /**
    * Stops this bundle with no options.
    */
   void stop() throws BundleException;

   /**
    * Update this bundle.
    */
   void update() throws BundleException;
}