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

import static org.jboss.osgi.spi.internal.SPILogger.LOGGER;
import static org.jboss.osgi.spi.internal.SPIMessages.MESSAGES;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.jboss.osgi.spi.framework.OSGiBootstrap;
import org.jboss.osgi.spi.framework.OSGiBootstrapProvider;
import org.jboss.osgi.spi.framework.PropertiesBootstrapProvider;
import org.jboss.osgi.spi.util.ServiceLoader;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;

/**
 * An internal bean that collabrates with {@link OSGiBootstrap}.
 * 
 * @author thomas.diesler@jboss.com
 * @since 04-Nov-2008
 */
public class OSGiBootstrapBean {

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

        Properties defaults = new Properties();

        LOGGER.debugf("JBoss OSGi System Properties");

        Enumeration<?> defaultNames = defaults.propertyNames();
        while (defaultNames.hasMoreElements()) {
            String propName = (String) defaultNames.nextElement();
            String sysValue = System.getProperty(propName);
            if (sysValue == null) {
                String propValue = defaults.getProperty(propName);
                System.setProperty(propName, propValue);
                LOGGER.debugf("   %s=%s", propName, propValue);
            }
        }
    }

    public static OSGiBootstrapProvider getBootstrapProvider() {

        OSGiBootstrapProvider provider = null;

        List<OSGiBootstrapProvider> providers = ServiceLoader.loadServices(OSGiBootstrapProvider.class);
        for (OSGiBootstrapProvider aux : providers) {
            try {
                aux.configure();
                provider = aux;
                break;
            } catch (Exception ex) {
                LOGGER.debugf(ex, "Cannot configure [%s]", aux.getClass().getName());
            }
        }

        if (provider == null) {
            provider = new PropertiesBootstrapProvider();
            LOGGER.debugf("Using default: %s", PropertiesBootstrapProvider.class.getName());
        }

        return provider;
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
                throw MESSAGES.illegalStateCannotStartFramework(ex);
            }

            float diff = (System.currentTimeMillis() - beforeStart) / 1000f;
            LOGGER.infoRuntimeBooted(diff);

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
            LOGGER.infoInitiatingShutdown();
            try {
                framework.stop();
                framework.waitForStop(5000);
            } catch (Exception ex) {
                LOGGER.errorCannotStopFramework(ex);
            }
            LOGGER.infoShutdownComplete();
        }
    }
}