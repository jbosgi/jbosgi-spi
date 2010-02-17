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

import java.util.List;

/**
 * An OSGi Service that gives access to Kernel bean registrations.
 * 
 * @author thomas.diesler@jboss.com
 * @since 23-Jan-2009
 */
public interface MicrocontainerService
{
   /**
    * The name under which the system bundle context is registered: 'jboss.osgi:service=BundleContext'
    */
   String BEAN_BUNDLE_CONTEXT = "jboss.osgi:service=BundleContext";

   /**
    * The name under which the MBeanServer is registered: 'jboss.osgi:service=MBeanServer'
    */
   String BEAN_MBEAN_SERVER = "jboss.osgi:service=MBeanServer";

   /**
    * The name under which the KernelController is registered: 'jboss.kernel:service=KernelController'
    */
   String BEAN_KERNEL_CONTROLLER = "jboss.kernel:service=KernelController";

   /**
    * The name under which the Kernel is registered: 'jboss.kernel:service=Kernel'
    */
   String BEAN_KERNEL = "jboss.kernel:service=Kernel";

   /**
    * Get the list of registered beans.
    */
   List<String> listRegisteredBeans();

   /**
    * Get a registered bean from the Kernel.
    * @return null if there is no bean registered under this name
    */
   Object getRegisteredBean(String beanName);

   /**
    * Get a registered bean from the Kernel.
    * @return null if there is no bean registered under this name
    */
   <T> T getRegisteredBean(Class<T> clazz, String beanName);
}