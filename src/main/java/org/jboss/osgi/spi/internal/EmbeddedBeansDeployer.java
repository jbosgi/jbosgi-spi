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
package org.jboss.osgi.spi.internal;

// $Id$

import java.net.URL;

import org.jboss.kernel.plugins.bootstrap.basic.BasicBootstrap;
import org.jboss.kernel.plugins.deployment.xml.BasicXMLDeployer;
import org.jboss.logging.Logger;

/**
 * Boostrap the Microcontainer
 * 
 * @author thomas.diesler@jboss.com
 * @since 27-Jun-2008
 */
public class EmbeddedBeansDeployer extends BasicBootstrap
{
  // Provide logging
  final Logger log = Logger.getLogger(EmbeddedBeansDeployer.class);

  private BasicXMLDeployer deployer;

  public EmbeddedBeansDeployer()
  {
    // Get or bootstrap the kernel
    if (getKernel() == null)
    {
      try
      {
        super.bootstrap();
        log.debug("bootstrap kernel: " + kernel);
      }
      catch (Throwable e)
      {
        throw new IllegalStateException("Cannot bootstrap kernel", e);
      }
    }
    deployer = new BasicXMLDeployer(kernel);
  }

  /**
   * Deploy MC beans from URL
   */
  public void deploy(URL url)
  {
    log.debug("deploy: " + url);
    try
    {
      deployer.deploy(url);
      deployer.validate();
    }
    catch (Throwable e)
    {
      throw new IllegalStateException("Cannot deploy beans from: " + url, e);
    }
  }

  /**
   * Undeploy MC beans from URL
   */
  public void undeploy(URL url)
  {
    log.debug("undeploy: " + url);
    try
    {
      deployer.undeploy(url);
    }
    catch (Throwable e)
    {
      throw new IllegalStateException("Cannot undeploy beans from: " + url, e);
    }
  }
}