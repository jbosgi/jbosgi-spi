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

import java.util.Map;

import org.jboss.osgi.spi.util.ServiceLoader;
import org.osgi.framework.Bundle;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

/**
 * The FrameworkLoader uses the {@link org.osgi.framework.launch} API to load a new 
 * instance on a {@link Framework}. 
 * 
 * @author thomas.diesler@jboss.com
 * @since 28-Jul-2009
 */
public abstract class FrameworkLoader 
{
   /**
    * Create a new {@link Framework} instance.
    * 
    * @param configuration The framework properties to configure the new
    *        framework instance. If framework properties are not provided by
    *        the configuration argument, the created framework instance must
    *        use some reasonable default configuration appropriate for the
    *        current VM. For example, the system packages for the current
    *        execution environment should be properly exported. The specified
    *        configuration argument may be <code>null</code>. The created
    *        framework instance must copy any information needed from the
    *        specified configuration argument since the configuration argument
    *        can be changed after the framework instance has been created.
    * @return A new, configured {@link Framework} instance. The framework
    *         instance must be in the {@link Bundle#INSTALLED} state.
    * @throws SecurityException If the caller does not have
    *         <code>AllPermission</code>, and the Java Runtime Environment
    *         supports permissions.
    */
   @SuppressWarnings("unchecked")
   public static Framework newFramework(Map configuration)
   {
      FrameworkFactory factory = ServiceLoader.loadService(FrameworkFactory.class);
      if (factory == null)
         throw new IllegalStateException("Cannot load FrameworkFactory");
      
      return factory.newFramework(configuration);
   }
   
}