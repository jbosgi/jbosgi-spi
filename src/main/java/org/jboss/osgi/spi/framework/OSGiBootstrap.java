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

import org.jboss.osgi.spi.internal.OSGiBootstrapBean;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

/**
 * The OSGiBootstrap is the entry point to obtain an {@link OSGiBootstrapProvider}.
 * 
 * A OSGiBootstrapProvider is discovered in two stages.
 * 
 * <ol>
 * <li>Read the bootstrap provider class name from a system property
 * <li>Read the bootstrap provider class name from a resource file
 * </ol>
 * 
 * In both cases the key is the fully qalified name of the {@link OSGiBootstrapProvider} interface.
 * 
 * @author thomas.diesler@jboss.com
 * @since 18-Jun-2008
 */
public class OSGiBootstrap
{
   /**
    * The main entry point
    */
   public static void main(String[] args)
   {
      OSGiBootstrapBean bean = new OSGiBootstrapBean();
      CmdLineParser parser = new CmdLineParser(bean);
      try
      {
         parser.parseArgument(args);
         bean.run();
      }
      catch (CmdLineException ex)
      {
         System.err.println("Invalid OSGiBootstrap options");
         parser.printUsage(System.err);
      }
   }

   /**
    * Get an instance of an OSGiBootstrapProvider.
    */
   public static OSGiBootstrapProvider getBootstrapProvider()
   {
      return OSGiBootstrapBean.getBootstrapProvider();
   }
}