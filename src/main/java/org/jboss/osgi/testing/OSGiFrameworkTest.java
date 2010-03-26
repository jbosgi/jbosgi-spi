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

// $Id: $

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jboss.logging.Logger;
import org.jboss.osgi.spi.framework.OSGiBootstrap;
import org.jboss.osgi.spi.framework.OSGiBootstrapProvider;
import org.jboss.osgi.spi.util.ConstantsHelper;
import org.jboss.osgi.vfs.AbstractVFS;
import org.jboss.osgi.vfs.VirtualFile;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
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

   protected static Framework framework;
   protected static BundleContext systemContext;

   private final List<FrameworkEvent> frameworkEvents = new CopyOnWriteArrayList<FrameworkEvent>();
   private final List<BundleEvent> bundleEvents = new CopyOnWriteArrayList<BundleEvent>();
   private final List<ServiceEvent> serviceEvents = new CopyOnWriteArrayList<ServiceEvent>();

   @BeforeClass
   public static void beforeClass() throws Exception
   {
      OSGiBootstrapProvider bootProvider = OSGiBootstrap.getBootstrapProvider();
      framework = bootProvider.getFramework();
      framework.start();

      // Get the system context
      systemContext = framework.getBundleContext();
   }

   @AfterClass
   public static void afterClass() throws Exception
   {
      if (framework != null)
      {
         framework.stop();
         framework.waitForStop(2000);
         framework = null;
      }
   }

   protected PackageAdmin getPackageAdmin()
   {
      ServiceReference sref = systemContext.getServiceReference(PackageAdmin.class.getName());
      return (PackageAdmin)systemContext.getService(sref);
   }
   
   protected Bundle installBundle(Archive<?> archive) throws BundleException, IOException
   {
      VirtualFile virtualFile = OSGiTestHelper.toVirtualFile(archive);
      return installBundle(archive.getName(), virtualFile.openStream());
   }
   
   protected Bundle installBundle(VirtualFile virtualFile) throws BundleException, IOException
   {
      String location = virtualFile.getPathName();
      return installBundle(location, virtualFile.openStream());
   }
   
   protected Bundle installBundle(String location) throws BundleException, IOException
   {
      URL bundleURL = getTestHelper().getTestArchiveURL(location);
      VirtualFile virtualFile = AbstractVFS.getRoot(bundleURL);
      return installBundle(location, virtualFile.openStream());
   }
   
   protected Bundle installBundle(String location, InputStream inputStream) throws BundleException
   {
      return systemContext.installBundle(location, inputStream);
   }
   
   protected void assertLoadClass(Bundle bundle, String className, Bundle exporter)
   {
      Class<?> clazz = assertLoadClass(bundle, className);
      Bundle actual = getPackageAdmin().getBundle(clazz);
      assertEquals("Loaded from ClassLoader", exporter, actual);
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
