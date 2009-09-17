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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.osgi.spi.framework.OSGiBootstrap;
import org.jboss.osgi.spi.framework.OSGiBootstrapProvider;
import org.jboss.osgi.spi.testing.internal.EmbeddedRuntime;
import org.jboss.osgi.spi.testing.internal.RemoteRuntime;

/**
 * An OSGi Test Helper
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 25-Sep-2008
 */
public class OSGiTestHelper
{
   private static final String SYSPROP_TEST_RESOURCES_DIRECTORY = "test.resources.directory";
   private static final String SYSPROP_TEST_ARCHIVE_DIRECTORY = "test.archive.directory";

   // The OSGiBootstrapProvider is a lazy property of the helper
   private OSGiBootstrapProvider bootProvider;
   private boolean skipCreateBootstrapProvider;

   private static String testResourcesDir;
   private static String testArchiveDir;

   public OSGiTestHelper()
   {
      testResourcesDir = System.getProperty(SYSPROP_TEST_RESOURCES_DIRECTORY, "target/test-classes");
      testArchiveDir = System.getProperty(SYSPROP_TEST_ARCHIVE_DIRECTORY, "target/test-libs");
   }

   public OSGiBootstrapProvider getBootstrapProvider()
   {
      if (bootProvider == null && skipCreateBootstrapProvider == false)
      {
         try
         {
            bootProvider = OSGiBootstrap.getBootstrapProvider();
         }
         catch (RuntimeException rte)
         {
            skipCreateBootstrapProvider = true;
            throw rte;
         }
      }
      return bootProvider;
   }

   public OSGiRuntime getDefaultRuntime()
   {
      OSGiRuntime runtime;

      String target = System.getProperty("target.container");
      if (target == null)
      {
         runtime = getEmbeddedRuntime();
      }
      else
      {
         runtime = getRemoteRuntime();
      }
      return runtime;
   }

   public OSGiRuntime getEmbeddedRuntime()
   {
      return new EmbeddedRuntime(this);
   }

   public OSGiRuntime getRemoteRuntime()
   {
      return new RemoteRuntime(this);
   }

   /** Try to discover the URL for the test resource */
   public URL getResourceURL(String resource)
   {
      URL resURL = null;
      try
      {
         File resourceFile = getResourceFile(resource);
         resURL = resourceFile.toURL();
      }
      catch (MalformedURLException e)
      {
         // ignore
      }
      return resURL;
   }

   /** Try to discover the File for the test resource */
   public File getResourceFile(String resource)
   {
      File file = new File(resource);
      if (file.exists())
         return file;

      file = new File(testResourcesDir + "/" + resource);
      if (file.exists())
         return file;

      throw new IllegalArgumentException("Cannot obtain '" + testResourcesDir + "/" + resource + "'");
   }

   /** Try to discover the URL for the deployment archive */
   public URL getTestArchiveURL(String archive)
   {
      try
      {
         return getTestArchiveFile(archive).toURL();
      }
      catch (MalformedURLException ex)
      {
         throw new IllegalStateException(ex);
      }
   }

   /** Try to discover the absolute path for the deployment archive */
   public String getTestArchivePath(String archive)
   {
      return getTestArchiveFile(archive).getAbsolutePath();
   }

   /** Try to discover the File for the deployment archive */
   public File getTestArchiveFile(String archive)
   {
      File file = new File(archive);
      if (file.exists())
         return file;

      file = new File(testArchiveDir + "/" + archive);
      if (file.exists())
         return file;

      throw new IllegalArgumentException("Cannot obtain '" + testArchiveDir + "/" + archive + "'.");
   }

   @SuppressWarnings("unchecked")
   public InitialContext getInitialContext() throws NamingException
   {
      Hashtable env = new Hashtable();
      env.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
      env.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
      env.put("java.naming.provider.url", "jnp://" + getServerHost() + ":" + getJndiPort());
      return new InitialContext(env);
   }

   public Integer getJndiPort()
   {
      String port = System.getProperty("jndi.server.port", "1099");
      return new Integer(port);
   }

   public String getServerHost()
   {
      return System.getProperty("jboss.bind.address", "localhost");
   }
   
   public String getTargetContainer()
   {
      return System.getProperty("target.container");
   }
   
   public String getFramework()
   {
      return System.getProperty("framework", "jbossmc");
   }
   
   public boolean isFrameworkEquinox()
   {
      return "equinox".equals(getFramework());
   }
   
   public boolean isFrameworkFelix()
   {
      return "felix".equals(getFramework());
   }
   
   public boolean isFrameworkJBossMC()
   {
      return "jbossmc".equals(getFramework());
   }
}
