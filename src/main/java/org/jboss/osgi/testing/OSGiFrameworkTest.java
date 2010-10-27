/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, JBoss Inc., and individual contributors as indicated
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


import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.logging.Logger;
import org.jboss.osgi.spi.framework.OSGiBootstrap;
import org.jboss.osgi.spi.framework.OSGiBootstrapProvider;
import org.jboss.osgi.spi.util.ConstantsHelper;
import org.jboss.osgi.testing.internal.EmbeddedRuntime;
import org.jboss.osgi.testing.internal.ManagementSupport;
import org.jboss.osgi.vfs.VirtualFile;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.framework.launch.Framework;
import org.osgi.jmx.framework.BundleStateMBean;
import org.osgi.jmx.framework.FrameworkMBean;
import org.osgi.jmx.framework.PackageStateMBean;
import org.osgi.jmx.framework.ServiceStateMBean;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * Parent for native framework tests.  
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 10-Mar-2010
 */
public abstract class OSGiFrameworkTest extends OSGiTest implements ServiceListener, SynchronousBundleListener, FrameworkListener
{
   // Provide logging
   private static final Logger log = Logger.getLogger(OSGiFrameworkTest.class);

   private static Framework framework;

   private final List<FrameworkEvent> frameworkEvents = new CopyOnWriteArrayList<FrameworkEvent>();
   private final List<BundleEvent> bundleEvents = new CopyOnWriteArrayList<BundleEvent>();
   private final List<ServiceEvent> serviceEvents = new CopyOnWriteArrayList<ServiceEvent>();

   private ManagementSupport jmxSupport;

   @Before
   public void setUp() throws Exception
   {
      super.setUp();
      if (framework == null && isBeforeClassPresent() == false)
      {
         createFramework();
         framework.start();
      }
   }

   @After
   public void tearDown() throws Exception
   {
      // Nothing to do if the framework was not created or shutdown already
      if (framework != null && framework.getState() == Bundle.ACTIVE)
      {
         // Report and cleanup left over files in the bundle stream dir
         File streamDir = new File("./target/osgi-store/bundle-0/bundle-streams");
         if (streamDir.exists() && streamDir.list().length > 0)
         {
            List<String> filelist = Arrays.asList(streamDir.list());
            System.err.println("Bundle streams not cleaned up: " + filelist);
         }
      }
      super.tearDown();
   }

   @AfterClass
   public static void afterClass() throws Exception
   {
      shutdownFramework();
   }

   public static Framework createFramework() throws BundleException
   {
      OSGiBootstrapProvider bootProvider = OSGiBootstrap.getBootstrapProvider();
      framework = bootProvider.getFramework();
      return framework;
   }

   public static Framework getFramework() throws BundleException
   {
      if (framework == null)
         throw new IllegalStateException("Framework not available. Use createFramework()");

      return framework;
   }

   public static void shutdownFramework() throws BundleException, InterruptedException
   {
      if (framework != null)
      {
         framework.stop();
         framework.waitForStop(2000);
         framework = null;
      }
   }

   public static BundleContext getSystemContext() throws BundleException
   {
      Framework framework = getFramework();
      if (framework.getState() != Bundle.ACTIVE)
         throw new IllegalStateException("Framework not ACTIVE. Did you start() the framework?");

      return framework.getBundleContext();
   }

   protected PackageAdmin getPackageAdmin() throws BundleException
   {
      BundleContext systemContext = getSystemContext();
      ServiceReference sref = systemContext.getServiceReference(PackageAdmin.class.getName());
      return (PackageAdmin)systemContext.getService(sref);
   }

   protected Bundle installBundle(Archive<?> archive) throws BundleException, IOException
   {
      return installBundle(archive.getName(), toInputStream(archive));
   }

   protected Bundle installBundle(VirtualFile virtualFile) throws BundleException, IOException
   {
      return getSystemContext().installBundle(virtualFile.getName(), virtualFile.openStream());
   }

   protected Bundle installBundle(String location) throws BundleException, IOException
   {
      try
      {
         new URL(location);
      }
      catch (Exception e)
      {
         location = getTestHelper().getTestArchivePath(location);
      }
      return getSystemContext().installBundle(location);
   }

   protected Bundle installBundle(String location, InputStream inputStream) throws BundleException
   {
      return getSystemContext().installBundle(location, inputStream);
   }

   @Override
   public void frameworkEvent(FrameworkEvent event)
   {
      synchronized (frameworkEvents)
      {
         log.debug("FrameworkEvent type=" + ConstantsHelper.frameworkEvent(event.getType()) + " for " + event);
         frameworkEvents.add(event);
         frameworkEvents.notifyAll();
      }
   }

   protected void assertNoFrameworkEvent() throws Exception
   {
      log.debug("frameworkEvents=" + frameworkEvents);
      assertEquals(0, frameworkEvents.size());
   }

   protected void assertFrameworkEvent(int type, Bundle bundle, Class<? extends Throwable> expectedThrowable) throws Exception
   {
      waitForEvent(frameworkEvents, type);
      log.debug("frameworkEvents=" + frameworkEvents);
      int size = frameworkEvents.size();
      assertTrue("" + size, size > 0);
      FrameworkEvent event = frameworkEvents.remove(0);
      assertEquals(ConstantsHelper.frameworkEvent(type), ConstantsHelper.frameworkEvent(event.getType()));
      Throwable t = event.getThrowable();
      if (expectedThrowable == null)
      {
         if (t != null)
         {
            log.error("Unexpected error in Framework event: ", t);
            fail("Unexpected throwable: " + t);
         }
      }
      else
      {
         String message = expectedThrowable.getSimpleName() + " is assignable from " + t.getClass().getSimpleName();
         assertTrue(message, expectedThrowable.isAssignableFrom(t.getClass()));
      }
      assertEquals(bundle, event.getSource());
      assertEquals(bundle, event.getBundle());
   }

   @Override
   public void bundleChanged(BundleEvent event)
   {
      synchronized (bundleEvents)
      {
         log.debug("BundleChanged type=" + ConstantsHelper.bundleEvent(event.getType()) + " for " + event);
         bundleEvents.add(event);
         bundleEvents.notifyAll();
      }
   }

   protected void assertNoBundleEvent() throws Exception
   {
      log.debug("bundleEvents=" + bundleEvents);
      assertEquals(0, bundleEvents.size());
   }

   protected void assertBundleEvent(int type, Bundle bundle) throws Exception
   {
      waitForEvent(bundleEvents, type);

      log.debug("bundleEvents=" + bundleEvents);
      int size = bundleEvents.size();
      assertTrue("" + size, size > 0);

      BundleEvent foundEvent = null;
      for (int i = 0; i < bundleEvents.size(); i++)
      {
         BundleEvent aux = bundleEvents.get(i);
         if (type == aux.getType())
         {
            if (bundle.equals(aux.getSource()) && bundle.equals(aux.getBundle()))
            {
               bundleEvents.remove(aux);
               foundEvent = aux;
               break;
            }
         }
      }

      if (foundEvent == null)
         fail("Cannot find event " + ConstantsHelper.bundleEvent(type) + " from " + bundle);
   }

   @Override
   public void serviceChanged(ServiceEvent event)
   {
      synchronized (serviceEvents)
      {
         log.debug("ServiceChanged type=" + ConstantsHelper.serviceEvent(event.getType()) + " for " + event);
         serviceEvents.add(event);
         serviceEvents.notifyAll();
      }
   }

   protected void assertNoServiceEvent() throws Exception
   {
      log.debug("serviceEvents=" + serviceEvents);
      assertEquals(0, serviceEvents.size());
   }

   protected void assertServiceEvent(int type, ServiceReference reference) throws Exception
   {
      waitForEvent(serviceEvents, type);
      log.debug("serviceEvents=" + serviceEvents);
      int size = serviceEvents.size();
      assertTrue("" + size, size > 0);
      ServiceEvent event = serviceEvents.remove(0);
      assertEquals(ConstantsHelper.serviceEvent(type), ConstantsHelper.serviceEvent(event.getType()));
      assertEquals(reference, event.getSource());
      assertEquals(reference, event.getServiceReference());
   }

   protected void assertNoAllReferences(BundleContext bundleContext, String clazz) throws Exception
   {
      assertNoAllReferences(bundleContext, clazz, null);
   }

   protected void assertNoAllReferences(BundleContext bundleContext, String clazz, String filter) throws Exception
   {
      ServiceReference[] actual = bundleContext.getAllServiceReferences(clazz, filter);
      if (actual != null)
         log.debug(bundleContext + " got " + Arrays.asList(actual) + " for clazz=" + clazz + " filter=" + filter);
      else
         log.debug(bundleContext + " got nothing for clazz=" + clazz + " filter=" + filter);
      assertNull("Expected no references for clazz=" + clazz + " filter=" + filter, actual);
   }

   protected void assertAllReferences(BundleContext bundleContext, String clazz, ServiceReference... expected) throws Exception
   {
      assertAllReferences(bundleContext, clazz, null, expected);
   }

   protected void assertAllReferences(BundleContext bundleContext, String clazz, String filter, ServiceReference... expected) throws Exception
   {
      ServiceReference[] actual = bundleContext.getAllServiceReferences(clazz, filter);
      if (actual != null)
         log.debug(bundleContext + " got " + Arrays.asList(actual) + " for clazz=" + clazz + " filter=" + filter);
      else
         log.debug(bundleContext + " got nothing for clazz=" + clazz + " filter=" + filter);
      assertArrayEquals(bundleContext + " with clazz=" + clazz + " filter=" + filter, expected, actual);
   }

   protected void assertNoReferences(BundleContext bundleContext, String clazz) throws Exception
   {
      assertNoReferences(bundleContext, clazz, null);
   }

   protected void assertNoReferences(BundleContext bundleContext, String clazz, String filter) throws Exception
   {
      ServiceReference[] actual = bundleContext.getServiceReferences(clazz, filter);
      if (actual != null)
         log.debug(bundleContext + " got " + Arrays.asList(actual) + " for clazz=" + clazz + " filter=" + filter);
      else
         log.debug(bundleContext + " got nothing for clazz=" + clazz + " filter=" + filter);
      assertNull("Expected no references for clazz=" + clazz + " filter=" + filter, actual);
   }

   protected void assertReferences(BundleContext bundleContext, String clazz, ServiceReference... expected) throws Exception
   {
      assertReferences(bundleContext, clazz, null, expected);
   }

   protected void assertReferences(BundleContext bundleContext, String clazz, String filter, ServiceReference... expected) throws Exception
   {
      ServiceReference[] actual = bundleContext.getServiceReferences(clazz, filter);
      if (actual != null)
         log.debug(bundleContext + " got " + Arrays.asList(actual) + " for clazz=" + clazz + " filter=" + filter);
      else
         log.debug(bundleContext + " got nothing for clazz=" + clazz + " filter=" + filter);
      assertArrayEquals(bundleContext + " with clazz=" + clazz + " filter=" + filter, expected, actual);
   }

   protected void assertNoGetReference(BundleContext bundleContext, String clazz) throws Exception
   {
      ServiceReference actual = bundleContext.getServiceReference(clazz);
      if (actual != null)
         log.debug(bundleContext + " got " + actual + " for clazz=" + clazz);
      else
         log.debug(bundleContext + " got nothing for clazz=" + clazz);
      assertNull("Expected no references for clazz=" + clazz, actual);
   }

   protected void assertGetReference(BundleContext bundleContext, String clazz, ServiceReference expected) throws Exception
   {
      ServiceReference actual = bundleContext.getServiceReference(clazz);
      if (actual != null)
         log.debug(bundleContext + " got " + Arrays.asList(actual) + " for clazz=" + clazz);
      else
         log.debug(bundleContext + " got nothing for clazz=" + clazz);
      assertEquals(bundleContext + " with clazz=" + clazz, expected, actual);
   }

   protected void assertUsingBundles(ServiceReference reference, Bundle... bundles)
   {
      Set<Bundle> actual = new HashSet<Bundle>();
      Bundle[] users = reference.getUsingBundles();
      if (users != null)
         actual.addAll(Arrays.asList(users));

      Set<Bundle> expected = new HashSet<Bundle>();
      expected.addAll(Arrays.asList(bundles));

      log.debug(reference + " users=" + actual);

      // switch - check expected on actual, since actual might be proxy
      assertEquals(actual, expected);
   }

   protected <T> T assertInstanceOf(Object o, Class<T> expectedType)
   {
      return assertInstanceOf(o, expectedType, false);
   }

   protected <T> T assertInstanceOf(Object o, Class<T> expectedType, boolean allowNull)
   {
      if (expectedType == null)
         fail("Null expectedType");

      if (o == null)
      {
         if (allowNull == false)
            fail("Null object not allowed.");
         else
            return null;
      }

      try
      {
         return expectedType.cast(o);
      }
      catch (ClassCastException e)
      {
         fail("Object " + o + " of class " + o.getClass().getName() + " is not an instanceof " + expectedType.getName());
         // should not reach this
         return null;
      }
   }

   protected MBeanServer getMBeanServer()
   {
      MBeanServer mbeanServer = EmbeddedRuntime.getLocalMBeanServer();
      return mbeanServer;
   }

   protected <T> T getMBeanProxy(ObjectName name, Class<T> interf)
   {
      return getJMXSupport().getMBeanProxy(name, interf);
   }

   protected FrameworkMBean getFrameworkMBean() throws IOException
   {
      return getJMXSupport().getFrameworkMBean();
   }

   protected BundleStateMBean getBundleStateMBean() throws IOException
   {
      return getJMXSupport().getBundleStateMBean();
   }

   protected PackageStateMBean getPackageStateMBean() throws IOException
   {
      return getJMXSupport().getPackageStateMBean();
   }

   protected ServiceStateMBean getServiceStateMBean() throws IOException
   {
      return getJMXSupport().getServiceStateMBean();
   }

   private ManagementSupport getJMXSupport()
   {
      if (jmxSupport == null)
         jmxSupport = new ManagementSupport(getMBeanServer());

      return jmxSupport;
   }

   /**
    * Get a ServiceReference within the given timeout.
    */
   protected ServiceReference getServiceReference(String clazz, long timeout) throws BundleException
   {
      int fraction = 200;
      timeout = timeout / fraction;
      BundleContext systemContext = getSystemContext();
      ServiceReference sref = systemContext.getServiceReference(clazz);
      while (sref == null && 0 < timeout--)
      {
         try
         {
            Thread.sleep(fraction);
         }
         catch (InterruptedException e)
         {
            // ignore
         }
         sref = systemContext.getServiceReference(clazz);
      }
      return sref;
   }

   protected void refreshPackages(Bundle[] bundles) throws Exception
   {
      final CountDownLatch latch = new CountDownLatch(1);
      FrameworkListener fl = new FrameworkListener()
      {
         @Override
         public void frameworkEvent(FrameworkEvent event)
         {
            if (event.getType() == FrameworkEvent.PACKAGES_REFRESHED)
               latch.countDown();
         }
      };

      BundleContext systemContext = getSystemContext();
      try
      {
         systemContext.addFrameworkListener(fl);
         getPackageAdmin().refreshPackages(bundles);
         assertTrue(latch.await(10, TimeUnit.SECONDS));
      }
      finally
      {
         systemContext.removeFrameworkListener(fl);
      }
   }

   @SuppressWarnings("rawtypes")
   private void waitForEvent(List events, int type) throws InterruptedException
   {
      // Timeout for event delivery: 3 sec 
      int timeout = 30;

      boolean eventFound = false;
      while (eventFound == false && 0 < timeout)
      {
         synchronized (events)
         {
            events.wait(100);
            for (Object aux : events)
            {
               if (aux instanceof BundleEvent)
               {
                  BundleEvent event = (BundleEvent)aux;
                  if (type == event.getType())
                  {
                     eventFound = true;
                     break;
                  }
               }
               else if (aux instanceof ServiceEvent)
               {
                  ServiceEvent event = (ServiceEvent)aux;
                  if (type == event.getType())
                  {
                     eventFound = true;
                     break;
                  }
               }
               else if (aux instanceof FrameworkEvent)
               {
                  FrameworkEvent event = (FrameworkEvent)aux;
                  if (type == event.getType())
                  {
                     eventFound = true;
                     break;
                  }
               }
            }
         }
         timeout--;
      }
   }
}
