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
package org.jboss.osgi.spi.internal;

import org.jboss.logging.Logger;
import org.jboss.osgi.spi.framework.OSGiBootstrap;
import org.jboss.osgi.spi.framework.OSGiBootstrapProvider;
import org.jboss.osgi.spi.framework.PropertiesBootstrapProvider;
import org.jboss.osgi.spi.util.ServiceLoader;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * An internal bean that collabrates with {@link OSGiBootstrap}.
 * 
 * @author thomas.diesler@jboss.com
 * @since 04-Nov-2008
 */
public class OSGiBootstrapBean {

    private static Logger log;

    public void run() {
        initBootstrap();

        OSGiBootstrapProvider bootProvider = getBootstrapProvider();
        Framework framework = bootProvider.getFramework();

        Runtime runtime = Runtime.getRuntime();
        runtime.addShutdownHook(new ShutdownThread(framework));

        Thread thread = new StartupThread(framework);
        thread.start();
    }

    private void initBootstrap() {

        // This property must be set before the logger is obtained
        log = Logger.getLogger(OSGiBootstrapBean.class);

        Properties defaults = new Properties();

        log.debug("JBoss OSGi System Properties");

        Enumeration<?> defaultNames = defaults.propertyNames();
        while (defaultNames.hasMoreElements()) {
            String propName = (String) defaultNames.nextElement();
            String sysValue = System.getProperty(propName);
            if (sysValue == null) {
                String propValue = defaults.getProperty(propName);
                System.setProperty(propName, propValue);
                log.debug("   " + propName + "=" + propValue);
            }
        }
    }

    public static OSGiBootstrapProvider getBootstrapProvider() {
        if (log == null)
            log = Logger.getLogger(OSGiBootstrap.class);

        OSGiBootstrapProvider provider = null;

        List<OSGiBootstrapProvider> providers = ServiceLoader.loadServices(OSGiBootstrapProvider.class);
        for (OSGiBootstrapProvider aux : providers) {
            try {
                aux.configure();
                provider = aux;
                break;
            } catch (Exception ex) {
                log.debug("Cannot configure [" + aux.getClass().getName() + "]", ex);
            }
        }

        if (provider == null) {
            provider = new PropertiesBootstrapProvider();
            log.debug("Using default: " + PropertiesBootstrapProvider.class.getName());
        }

        return provider;
    }

    private URL toURL(File file) {
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid file: " + file);
        }
    }

    class StartupThread extends Thread {

        private Framework framework;

        public StartupThread(Framework framework) {
            this.framework = framework;
        }

        public void run() {
            // Start the framework
            long beforeStart = System.currentTimeMillis();
            try {
                framework.start();
            } catch (BundleException ex) {
                throw new IllegalStateException("Cannot start framework", ex);
            }

            float diff = (System.currentTimeMillis() - beforeStart) / 1000f;
            log.info("JBossOSGi Runtime booted in " + diff + "sec");

            Reader br = new InputStreamReader(System.in);
            try {
                int inByte = br.read();
                while (inByte != -1) {
                    inByte = br.read();
                }
            } catch (IOException ioe) {
                // ignore user input
            }
        }
    }

    class ShutdownThread extends Thread {

        private Framework framework;

        public ShutdownThread(Framework framework) {
            this.framework = framework;
        }

        public void run() {
            log.info("Initiating shutdown ...");
            try {
                framework.stop();
                framework.waitForStop(5000);
            } catch (Exception ex) {
                log.error("Cannot stop framework", ex);
            }
            log.info("Shutdown complete");
        }
    }
}