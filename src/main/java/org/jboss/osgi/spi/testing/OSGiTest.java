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
package org.jboss.osgi.spi.testing;

import java.io.File;
import java.net.URL;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.logging.Logger;
import org.jboss.virtual.VFS;
import org.junit.After;
import org.junit.Before;

/**
 * An abstract OSGi Test.
 * 
 * {@link OSGiTest} is a convenience wrapper for the functionality provided 
 * by {@link OSGiTestHelper}. 
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 25-Sep-2008
 */
public abstract class OSGiTest 
{
   // Provide logging
   final Logger log = Logger.getLogger(OSGiTest.class);

   private OSGiTestHelper helper;

   protected OSGiTest()
   {
      // Prevent unknown protocol: vfsfile
      VFS.init();
   }

   /**
    * Get the test helper used by this test
    * 
    * Overwrite if you need to supply another helper
    * i.e. one that you have statically setup 
    */
   protected OSGiTestHelper getTestHelper()
   {
      if (helper == null)
         helper = new OSGiTestHelper();
         
      return helper;
   }

   /**
    * Writes a a debug start messge
    */
   @Before
   public void setUp() throws Exception
   {
      log.debug("### START " + getLongName());
   }

   /**
    * Writes a a debug stop messge
    */
   @After
   public void tearDown() throws Exception
   {
      log.debug("### END " + getLongName());
   }

   /**
    * Get the last token in the FQN of this test class. 
    */
   protected String getShortName()
   {
      String shortName = getClass().getName();
      shortName = shortName.substring(shortName.lastIndexOf(".") + 1);
      return shortName;
   }

   /**
    * Get the the FQN of this test class. 
    */
   protected String getLongName()
   {
      return getClass().getName();
   }

   /**
    * Delegates to {@link OSGiTestHelper#getDefaultRuntime()}
    */
   protected OSGiRuntime getDefaultRuntime()
   {
      return getTestHelper().getDefaultRuntime();
   }

   /**
    * Delegates to {@link OSGiTestHelper#getEmbeddedRuntime()}
    */
   protected OSGiRuntime getEmbeddedRuntime()
   {
      return getTestHelper().getEmbeddedRuntime();
   }

   /**
    * Delegates to {@link OSGiTestHelper#getRemoteRuntime()}
    */
   public OSGiRuntime getRemoteRuntime()
   {
      return getTestHelper().getRemoteRuntime();
   }

   /**
    * Delegates to {@link OSGiTestHelper#getResourceURL(String)}
    */
   protected URL getResourceURL(String resource)
   {
      return getTestHelper().getResourceURL(resource);
   }

   /**
    * Delegates to {@link OSGiTestHelper#getResourceFile(String)}
    */
   protected File getResourceFile(String resource)
   {
      return getTestHelper().getResourceFile(resource);
   }

   /**
    * Delegates to {@link OSGiTestHelper#getTestArchiveURL(String)}
    */
   protected URL getTestArchiveURL(String archive)
   {
      return getTestHelper().getTestArchiveURL(archive);
   }

   /**
    * Delegates to {@link OSGiTestHelper#getTestArchivePath(String)}
    */
   protected String getTestArchivePath(String archive)
   {
      return getTestHelper().getTestArchivePath(archive);
   }

   /**
    * Delegates to {@link OSGiTestHelper#getTestArchiveFile(String)}
    */
   protected File getTestArchiveFile(String archive)
   {
      return getTestHelper().getTestArchiveFile(archive);
   }
   
   /**
    * Delegates to {@link OSGiTestHelper#getInitialContext()}
    */
   public InitialContext getInitialContext() throws NamingException
   {
      return getTestHelper().getInitialContext();
   }

   /**
    * Delegates to {@link OSGiTestHelper#getJndiPort()}
    */
   public Integer getJndiPort()
   {
      return getTestHelper().getJndiPort();
   }

   /**
    * Delegates to {@link OSGiTestHelper#getServerHost()}
    */
   public String getServerHost()
   {
      return getTestHelper().getServerHost();
   }
   
   /**
    * Delegates to {@link OSGiTestHelper#getTargetContainer()}
    */
   public String getTargetContainer()
   {
      return getTestHelper().getTargetContainer();
   }
   
   /**
    * Delegates to {@link OSGiTestHelper#getFramework()}
    */
   public String getFramework()
   {
      return getTestHelper().getFramework();
   }
   
   /**
    * Delegates to {@link OSGiTestHelper#isFrameworkEquinox()}
    */
   public boolean isFrameworkEquinox()
   {
      return getTestHelper().isFrameworkEquinox();
   }
   
   /**
    * Delegates to {@link OSGiTestHelper#isFrameworkFelix()}
    */
   public boolean isFrameworkFelix()
   {
      return getTestHelper().isFrameworkFelix();
   }
   
   /**
    * Delegates to {@link OSGiTestHelper#isFrameworkJBossMC()}
    */
   public boolean isFrameworkJBossMC()
   {
      return getTestHelper().isFrameworkJBossMC();
   }
}
