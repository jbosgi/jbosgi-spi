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
   private OSGiRuntimeHelper helper;

   /**
    * Get the test helper used by this test
    * 
    * Overwrite if you need to supply another helper
    * i.e. one that you have statically setup 
    */
   protected OSGiRuntimeHelper getRuntimeHelper()
   {
      if (helper == null)
         helper = new OSGiRuntimeHelper();

      return helper;
   }

   /**
    * Delegates to {@link OSGiRuntimeHelper#getDefaultRuntime()}
    */
   protected OSGiRuntime getDefaultRuntime()
   {
      return getRuntimeHelper().getDefaultRuntime();
   }

   /**
    * Delegates to {@link OSGiRuntimeHelper#getEmbeddedRuntime()}
    */
   protected OSGiRuntime getEmbeddedRuntime()
   {
      return getRuntimeHelper().getEmbeddedRuntime();
   }

   /**
    * Delegates to {@link OSGiRuntimeHelper#getRemoteRuntime()}
    */
   public OSGiRuntime getRemoteRuntime()
   {
      return getRuntimeHelper().getRemoteRuntime();
   }
}
