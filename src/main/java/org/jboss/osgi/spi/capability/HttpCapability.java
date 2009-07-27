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
package org.jboss.osgi.spi.capability;

//$Id$

import java.util.Properties;

import org.jboss.osgi.spi.testing.OSGiRuntime;
import org.osgi.service.http.HttpService;

/**
 * Adds the {@link HttpService} capability to the {@link OSGiRuntime}
 * under test. 
 * 
 * It is ignored if the {@link HttpService} is already registered.
 * 
 * Installed bundles: org.apache.felix.http.jetty.jar
 * 
 * Default properties set by this capability
 * 
 * <table>
 * <tr><th>Property</th><th>Value</th></tr> 
 * <tr><td>org.osgi.service.http.port</td><td>8090</td></tr> 
 * </table> 
 * 
 * @author thomas.diesler@jboss.com
 * @since 05-May-2009
 */
public class HttpCapability extends Capability
{
   public HttpCapability()
   {
      super(HttpService.class.getName());
      
      Properties props = getProperties();
      props.setProperty("org.osgi.service.http.port", "8090");

      addBundle("bundles/org.apache.felix.http.jetty.jar");
   }
}