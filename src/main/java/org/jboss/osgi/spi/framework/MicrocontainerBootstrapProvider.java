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

import java.io.InputStream;
import java.net.URL;

import org.jboss.dependency.spi.Controller;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.kernel.plugins.bootstrap.basic.KernelConstants;
import org.jboss.osgi.spi.NotImplementedException;
import org.jboss.osgi.spi.internal.EmbeddedBeansDeployer;

/**
 * Bootstrap the OSGiFramework through the MC
 *
 * <pre>
 *   &lt;deployment xmlns="urn:jboss:bean-deployer:2.0" ...>
 *    
 *      &lt;!-- The OSGiFramework -->
 *      &lt;bean name="jboss.osgi:service=Framework" class="org.jboss.osgi.felix.framework.FelixIntegration">
 *       &lt;property name="felixProperties">
 *        &lt;map keyClass="java.lang.String" valueClass="java.lang.String">
 *         &lt;entry>&lt;key>org.osgi.framework.storage.clean&lt;/key>&lt;value>onFirstInit&lt;/value>&lt;/entry>
 *         &lt;entry>
 *           &lt;key>org.osgi.framework.system.packages&lt;/key>
 *           &lt;value>
 *             org.osgi.framework; version=1.4,
 *             org.osgi.util.tracker
 *           &lt;/value>
 *         &lt;/entry>
 *        &lt;/map>
 *       &lt;/property>
 *      &lt;/bean>
 *      ...
 *    &lt;/deployment>
 * </pre>
 * 
 * @author thomas.diesler@jboss.com
 * @since 18-Jun-2008
 */
public class MicrocontainerBootstrapProvider implements OSGiBootstrapProvider
{
   /** The default framework beans property: jboss.osgi.bootstrap.beans */
   public static final String OSGI_BOOTSTRAP_BEANS = "jboss.osgi.bootstrap.beans";
   /** The default framework beans: jboss-osgi-bootstrap-beans.xml */
   public static final String DEFAULT_OSGI_BOOTSTRAP_XML = "jboss-osgi-bootstrap-beans.xml";
   /** The default framework beans property: jboss.osgi.framework.beans */
   public static final String OSGI_FRAMEWORK_BEANS = "jboss.osgi.framework.beans";
   /** The default framework beans: jboss-osgi-beans.xml */
   public static final String DEFAULT_OSGI_FRAMEWORK_XML = "jboss-osgi-beans.xml";
   
   private EmbeddedBeansDeployer deployer = new EmbeddedBeansDeployer();
   private boolean bootstraped;
   private boolean configured;

   public OSGiFramework getFramework()
   {
      return getFramework(DEFAULT_FRAMEWORK_NAME);
   }

   public OSGiFramework getFramework(String beanName)
   {
      OSGiFramework framework = getInstance(beanName, OSGiFramework.class);
      if (framework == null && configured == false)
      {
         configureWithDefaultBeans();
         framework = getInstance(beanName, OSGiFramework.class);
      }
      return framework;
   }

   public void configure()
   {
      configureWithDefaultBeans();
   }

   public void configure(InputStream streamConfig)
   {
      throw new NotImplementedException("Cannot bootstrap JBossMC from InputStream");
   }

   public void configure(String resourceConfig)
   {
      URL urlConfig = Thread.currentThread().getContextClassLoader().getResource(resourceConfig);
      if (urlConfig == null)
         throw new IllegalStateException("Cannot find resource: " + resourceConfig);

      configure(urlConfig);
   }

   public void configure(URL urlConfig)
   {
      deployer.deploy(urlConfig);
      configured = true;
   }

   public Object getInstance(String name)
   {
      Object retObj = null;

      if (bootstraped == false)
      {
         bootstrapKernel();
         bootstraped = true;
      }

      if (KernelConstants.KERNEL_NAME.equals(name))
      {
         retObj = deployer.getKernel();
      }
      else
      {
         Controller controller = deployer.getKernel().getController();
         ControllerContext context = controller.getInstalledContext(name);
         if (context != null)
            retObj = context.getTarget();
      }

      return retObj;
   }

   public <T> T getInstance(String name, Class<T> expectedType)
   {
      if (name == null)
         throw new IllegalArgumentException("Null name");

      if (expectedType == null)
         throw new IllegalArgumentException("Null expected type.");

      Object attribute = getInstance(name);
      if (attribute != null)
      {
         if (expectedType.isInstance(attribute) == false)
            throw new IllegalArgumentException("Not of expected type [" + expectedType + "]: " + attribute);

         return expectedType.cast(attribute);
      }
      return null;
   }
   
   private void configureWithDefaultBeans()
   {
      String defaultFrameworkBeans = System.getProperty(OSGI_FRAMEWORK_BEANS, DEFAULT_OSGI_FRAMEWORK_XML);
      configure(defaultFrameworkBeans);
   }

   private void bootstrapKernel()
   {
      // Deploy the bootstrap beans if DEFAULT_BOOTSTRAP_BEANS is on the classpath
      String defaultBootstrapBeans = System.getProperty(OSGI_BOOTSTRAP_BEANS, DEFAULT_OSGI_BOOTSTRAP_XML);
      URL urlBootstrap = Thread.currentThread().getContextClassLoader().getResource(defaultBootstrapBeans);
      if (urlBootstrap != null)
         deployer.deploy(urlBootstrap);
   }
}