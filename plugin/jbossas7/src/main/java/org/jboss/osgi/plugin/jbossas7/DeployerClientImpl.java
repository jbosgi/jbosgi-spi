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
package org.jboss.osgi.plugin.jbossas7;

import java.net.URL;

import org.jboss.osgi.spi.NotImplementedException;
import org.jboss.osgi.testing.OSGiDeployerClient;
import org.jboss.osgi.testing.OSGiRuntime;
import org.osgi.framework.BundleException;

/**
 * An abstract deployer for the {@link OSGiRuntime}
 *
 * @author Thomas.Diesler@jboss.org
 * @since 09-Nov-2010
 */
public class DeployerClientImpl implements OSGiDeployerClient
{
   @Override
   public void deploy(URL url) throws BundleException
   {
      throw new NotImplementedException();
   }

   @Override
   public void undeploy(URL url) throws BundleException
   {
      throw new NotImplementedException();
   }
}