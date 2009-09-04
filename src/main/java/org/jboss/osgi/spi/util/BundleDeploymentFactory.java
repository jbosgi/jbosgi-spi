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
package org.jboss.osgi.spi.util;

//$Id: BundleDeployment.java 90925 2009-07-08 10:12:31Z thomas.diesler@jboss.com $

import java.io.IOException;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;

/**
 * A factory for bundle deployments.
 * 
 * @author thomas.diesler@jboss.com
 * @since 08-Jul-2009
 */
public abstract class BundleDeploymentFactory 
{
   public static BundleDeployment createBundleDeployment(URL url) throws BundleException
   {
      Manifest manifest;
      try
      {
         JarFile jarFile = new JarFile(url.getPath());
         manifest = jarFile.getManifest();
         jarFile.close();
      }
      catch (IOException ex)
      {
         throw new BundleException("Cannot get manifest from: " + url);

      }

      Attributes attribs = manifest.getMainAttributes();
      String symbolicName = attribs.getValue(Constants.BUNDLE_SYMBOLICNAME);
      if (symbolicName == null)
         throw new BundleException("Cannot obtain Bundle-SymbolicName for: " + url);

      String versionStr = attribs.getValue(Constants.BUNDLE_VERSION);
      Version version = Version.parseVersion(versionStr);
      return new BundleDeployment(url, symbolicName, version);
   }
}