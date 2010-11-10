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
package org.jboss.osgi.plugin.jbossas7;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import org.jboss.as.standalone.client.api.StandaloneClient;
import org.jboss.as.standalone.client.api.deployment.DeploymentAction;
import org.jboss.as.standalone.client.api.deployment.DeploymentPlan;
import org.jboss.as.standalone.client.api.deployment.DeploymentPlanBuilder;
import org.jboss.as.standalone.client.api.deployment.ServerDeploymentActionResult;
import org.jboss.as.standalone.client.api.deployment.ServerDeploymentManager;
import org.jboss.as.standalone.client.api.deployment.ServerDeploymentPlanResult;
import org.jboss.logging.Logger;
import org.jboss.osgi.testing.OSGiDeployerClient;
import org.jboss.osgi.testing.OSGiRuntime;
import org.osgi.framework.BundleException;

/**
 * An abstract deployer for the {@link OSGiRuntime}
 *
 * @author Thomas.Diesler@jboss.org
 * @since 09-Nov-2010
 */
public class DeployerClientImpl implements OSGiDeployerClient
{
   // Provide logging
   private static final Logger log = Logger.getLogger(DeployerClientImpl.class);

   private ServerDeploymentManager deploymentManager;
   private Map<String, String> registry = new HashMap<String, String>();

   public DeployerClientImpl() throws IOException
   {
      InetAddress address = InetAddress.getByName("127.0.0.1");
      StandaloneClient client = StandaloneClient.Factory.create(address, 9999);
      deploymentManager = client.getDeploymentManager();
   }

   @Override
   public void deploy(URL url) throws BundleException, IOException
   {
      try
      {
         DeploymentPlanBuilder builder = deploymentManager.newDeploymentPlan();
         builder = builder.add(url).andDeploy();

         DeploymentPlan plan = builder.build();
         DeploymentAction deployAction = builder.getLastAction();
         Future<ServerDeploymentPlanResult> future = deploymentManager.execute(plan);
         ServerDeploymentPlanResult planResult = future.get();

         ServerDeploymentActionResult actionResult = planResult.getDeploymentActionResult(deployAction.getId());
         Throwable deploymentException = actionResult.getDeploymentException();
         if (deploymentException != null)
            throw deploymentException;

         registry.put(url.toExternalForm(), deployAction.getDeploymentUnitUniqueName());
      }
      catch (Throwable ex)
      {
         throw new BundleException("Cannot deploy: " + url, ex);
      }
   }

   @Override
   public void undeploy(URL url) throws BundleException
   {
      try
      {
         String uiqueName = registry.remove(url.toExternalForm());
         if (uiqueName != null)
         {
            DeploymentPlanBuilder builder = deploymentManager.newDeploymentPlan();
            DeploymentPlan plan = builder.undeploy(uiqueName).remove(uiqueName).build();
            Future<ServerDeploymentPlanResult> future = deploymentManager.execute(plan);
            future.get();
         }
      }
      catch (Throwable ex)
      {
         log.warn("Cannot undeploy: " + url, ex);
      }
   }
}