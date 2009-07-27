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
package org.jboss.osgi.spi.framework;

//$Id$

import java.net.URL;
import java.util.List;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * An abstraction of an OSGi Framework
 * 
 * @author thomas.diesler@jboss.com
 * @since 23-Jan-2009
 */
public interface OSGiFramework
{
   /*
    * * Get the Framework properties
    */
   Map<String, Object> getProperties();

   /*
    * * Set the Framework properties. This can only be done before the Framework is started.
    */
   void setProperties(Map<String, Object> props);

   /*
    * * Get the list of bundles that get installed automatically
    */
   List<URL> getAutoInstall();

   /*
    * * Set the list of bundles that get installed automatically. This can only be done before the Framework is started.
    */
   void setAutoInstall(List<URL> autoInstall);

   /*
    * * Get the list of bundles that get installed and started automatically
    */
   List<URL> getAutoStart();

   /*
    * * Set the list of bundles that get installed and started automatically. This can only be done before the Framework is started.
    */
   void setAutoStart(List<URL> autoStart);

   /*
    * * Start the Framework
    */
   void start();

   /*
    * * Stop the Framework
    */
   void stop();

   /*
    * * Get the System Bundle associated with this Framework
    */
   Bundle getBundle();

   /*
    * * Get the System Bundle Context associated with this Framework
    */
   BundleContext getBundleContext();
}