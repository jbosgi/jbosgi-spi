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

//$Id$

import java.net.URL;

import javax.management.ObjectName;

import org.jboss.osgi.spi.management.ObjectNameFactory;
import org.jboss.osgi.spi.util.BundleDeployment;
import org.osgi.framework.BundleException;

/**
 * A Service that can be used to deploy/undeploy bundles or archives to/from the runtime.
 * 
 * @author thomas.diesler@jboss.com
 * @since 23-Jan-2009
 */
public interface DeployerService
{
   /**
    * The object name under which this is registered: 'jboss.osgi:service=DeployerService'
    */
   ObjectName MBEAN_DEPLOYER_SERVICE = ObjectNameFactory.create("jboss.osgi:service=DeployerService");

   /**
    * Deploy an array of bundles
    */
   void deploy(BundleDeployment[] bundleDeps) throws BundleException;

   /**
    * Undeploy an array of bundles
    */
   void undeploy(BundleDeployment[] bundleDeps) throws BundleException;

   /**
    * Deploy bundle from URL
    */
   void deploy(URL url) throws BundleException;

   /**
    * Undeploy bundle from URL.
    * 
    * Note, due to the dynamic nature of OSGi services it is 
    * possible that a {@link DeployerService} is asked to undeploy 
    * a bundle URL which it did not deploy itself.
    * 
    * In this case this method should return false, so that  
    * another {@link DeployerService} can be tried.
    * 
    * @return true if this service could undeploy the bundle
    */
   boolean undeploy(URL url) throws BundleException;
}