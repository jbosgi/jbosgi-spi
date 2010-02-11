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

//$Id$

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.logging.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * A helper the logs the exported packages for a bundle.
 * 
 * @author thomas.diesler@jboss.com
 * @since 24-Apr-2009
 */
public final class ExportedPackageHelper
{
   // Provide logging
   private static final Logger log = Logger.getLogger(ExportedPackageHelper.class);

   private PackageAdmin packageAdmin;

   public ExportedPackageHelper(BundleContext context)
   {
      ServiceReference sref = context.getServiceReference(PackageAdmin.class.getName());
      if (sref != null)
         packageAdmin = (PackageAdmin)context.getService(sref);
   }

   public boolean resolveBundle(Bundle bundle)
   {
      return packageAdmin != null ? packageAdmin.resolveBundles(new Bundle[] { bundle }) : false;
   }

   public boolean resolveBundles(Bundle[] bundles)
   {
      return packageAdmin != null ? packageAdmin.resolveBundles(bundles) : false;
   }

   /*
    * * Log the list of exported packages
    */
   public void logExportedPackages(Bundle bundle)
   {
      if (packageAdmin != null)
      {
         log.debug("Exported-Packages: " + bundle.getSymbolicName());
         
         ExportedPackage[] exportedPackages = packageAdmin.getExportedPackages(bundle);
         if (exportedPackages != null)
         {
            List<String> packages = new ArrayList<String>();
            for (ExportedPackage exp : exportedPackages)
               packages.add("  " + exp.getName() + ";version=" + exp.getVersion());
            
            Collections.sort(packages);
            for (String exp : packages)
               log.debug(exp);
         }
      }
   }
}