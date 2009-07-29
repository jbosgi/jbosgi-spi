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

import java.io.InputStream;
import java.net.URL;

import org.osgi.framework.launch.Framework;

/**
 * An OSGiBootstrapProvider provides an OSGi Framework
 * <p/>
 * Implementations would configure the OSGi Framework through some form of descriptor.
 * 
 * @author thomas.diesler@jboss.com
 * @since 18-Jun-2008
 */
public interface OSGiBootstrapProvider
{
   /**
    * Get the configured OSGi Framework
    * 
    * @return The configured instance of a Framework
    */
   Framework getFramework();

   /**
    * Configure this provider with the default configuration
    */
   void configure();

   /**
    * Configure this provider from the given URL
    */
   void configure(URL urlConfig);

   /**
    * Configure this provider from a given resource
    */
   void configure(String resourceConfig);

   /**
    * Configure this provider from a given input stream
    */
   void configure(InputStream streamConfig);
}