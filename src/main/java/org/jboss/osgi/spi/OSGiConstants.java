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
package org.jboss.osgi.spi;

//$Id$

/**
 * JBossOSGi Constants
 * 
 * @author thomas.diesler@jboss.com
 * @since 04-Mar-2009
 */
public interface OSGiConstants
{
   /** The JBossOSGi domain 'jboss.osgi' */
   String DOMAIN_NAME = "jboss.osgi";
   
   /**
    * If set to 'true' bundles can be deployed in any order. Deployed bundle will get started when their dependencies can be resolved.
    * If set to 'false' bundles must be deployed in the order that is required to start them.
    * 
    * The default is 'true' 
    */
   String PROPERTY_DEFERRED_START = "org.jboss.osgi.deferred.start";

   /**
    * If set to 'true' bundles are started automatically.
    * 
    * The default is 'false' 
    */
   String PROPERTY_AUTO_START = "org.jboss.osgi.auto.start";

   /**
    * Specifies the start level for a bundle.
    * 
    * The default is '0' 
    */
   String PROPERTY_START_LEVEL = "org.jboss.osgi.start.level";

   /**
    * The JBossOSGi runtime system property that denotes the path to the runtime
    */
   String OSGI_HOME = "osgi.home";

   /**
    * The JBossOSGi runtime system property that denotes the path to the active runtime profile
    */
   String OSGI_SERVER_HOME = "osgi.server.home";

   /**
    * A JBossOSGi deployment unit attachment key that containe the bundle's symbolic name. 
    */
   String KEY_BUNDLE_SYMBOLIC_NAME = "org.jboss.osgi.bundle.symbolic.name";

}
