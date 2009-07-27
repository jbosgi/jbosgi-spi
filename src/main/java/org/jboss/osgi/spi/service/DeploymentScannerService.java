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
package org.jboss.osgi.spi.service;

//$Id$

import java.net.URL;

import org.jboss.osgi.spi.util.BundleDeployment;

/**
 * A service that scans a directory location for new/removed bundles.
 * 
 * @author thomas.diesler@jboss.com
 * @since 27-May-2009
 */
public interface DeploymentScannerService
{
   /**
    * The property that names the scan location: org.jboss.osgi.hotdeploy.scandir
    */
   String PROPERTY_SCAN_LOCATION = "org.jboss.osgi.hotdeploy.scandir";
   
   /**
    * The property to defines the scan interval: org.jboss.osgi.hotdeploy.interval
    */
   String PROPERTY_SCAN_INTERVAL = "org.jboss.osgi.hotdeploy.interval";
   
   /**
    * Get the scan location URL.
    *  
    * This is can be specified by setting the {@link #PROPERTY_SCAN_LOCATION} property. 
    */
   URL getScanLocation();
   
   /**
    * The number of scans since the service started 
    */
   long getScanCount();
   
   /**
    * The number of milliseconds between scans
    * Defaults to 2000ms 
    */
   long getScanInterval();
   
   /**
    * The timestamp of the last change
    */
   long getLastChange();
   
   /**
    * Run a directory scan
    */
   void scan();
   
   /**
    * Returns the array of bundles currently known to the deployemtn scanner. 
    */
   BundleDeployment[] getBundleDeployments();
   
   /**
    * Add a scan listener to this service
    */
   void addScanListener(ScanListener listener);
   
   /**
    * Remove the scan listener from this service
    */
   void removeScanListener(ScanListener listener);
   
   /**
    * A ScanListener can be registered with the {@link DeploymentScannerService}
    * to monitor the progress of deployed bundles 
    */
   interface ScanListener
   {
      /**
       * Called before every scan
       */
      void beforeScan(DeploymentScannerService service);
      
      /**
       * Called after every scan
       */
      void afterScan(DeploymentScannerService service);
   }
}