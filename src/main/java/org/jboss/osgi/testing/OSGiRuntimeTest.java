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
package org.jboss.osgi.testing;

import org.jboss.osgi.testing.internal.EmbeddedRuntimeImpl;
import org.jboss.osgi.testing.internal.RemoteRuntimeImpl;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;

/**
 * An abstract OSGi runtime test.
 * 
 * A convenience wrapper for the functionality provided by {@link OSGiRuntimeHelper}. 
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 25-Sep-2008
 */
public abstract class OSGiRuntimeTest extends OSGiTest
{
   private static OSGiRuntime runtime;

   @Before
   public void setUp() throws Exception
   {
      super.setUp();

      if (runtime == null && isBeforeClassPresent() == false)
         runtime = createDefaultRuntime();
   }

   @After
   public void tearDown() throws Exception
   {
      //getRuntime().refreshPackages(null);
      super.tearDown();
   }
   
   @AfterClass
   public static void afterClass() throws Exception
   {
      shutdownRuntime();
   }

   public static void shutdownRuntime()
   {
      // Nothing to do if the runtime was not created
      if (runtime != null)
      {
         runtime.shutdown();
         runtime = null;
      }
   }
   
   public static OSGiRuntime getRuntime() 
   {
      if (runtime == null)
         throw new IllegalStateException("OSGiRuntime not available. Use createRuntime()");

      return runtime;
   }

   /**
    * Delegates to {@link OSGiRuntimeHelper#getDefaultRuntime()}
    */
   public static OSGiRuntime createDefaultRuntime()
   {
      String target = System.getProperty("target.container");
      if (target == null)
      {
         return createEmbeddedRuntime();
      }
      else
      {
         return createRemoteRuntime();
      }
   }

   /**
    * Delegates to {@link OSGiRuntimeHelper#getEmbeddedRuntime()}
    */
   public static OSGiRuntime createEmbeddedRuntime()
   {
      runtime = new EmbeddedRuntimeImpl(new OSGiRuntimeHelper());
      return runtime;
   }

   /**
    * Delegates to {@link OSGiRuntimeHelper#getRemoteRuntime()}
    */
   public static OSGiRuntime createRemoteRuntime()
   {
      runtime = new RemoteRuntimeImpl(new OSGiRuntimeHelper());
      return runtime;
   }
}
