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
package org.jboss.osgi.spi.service;

//$Id: DeployerService.java 90894 2009-07-07 11:58:40Z thomas.diesler@jboss.com $

import java.net.URL;

import org.jboss.osgi.spi.util.BundleDeployment;
import org.osgi.framework.Version;

/**
 * A Service to register/unregister bundle deployments.
 * 
 * @author thomas.diesler@jboss.com
 * @since 08-Jul-2009
 */
public interface DeploymentRegistryService
{
   /**
    * Create a bundle deployment from the given bundle URL
    * @return null, if this service does not maintain the bundle deployment
    */
   BundleDeployment getBundleDeployment(URL url);
   
   /**
    * Get the bundle deployment for the given bundle symbolicName and version
    * @return null, if this service does not maintain the bundle deployment
    */
   BundleDeployment getBundleDeployment(String symbolicName, Version version);

   /**
    * Register a bundle deployment
    */
   void registerBundleDeployment(BundleDeployment dep);
   
   /**
    * Unregister a bundle deployment
    */
   void unregisterBundleDeployment(BundleDeployment dep);
}