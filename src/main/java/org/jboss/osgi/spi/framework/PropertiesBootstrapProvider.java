/*
 * #%L
 * JBossOSGi SPI
 * %%
 * Copyright (C) 2010 - 2012 JBoss by Red Hat
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
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

import static org.jboss.osgi.spi.SPILogger.LOGGER;
import static org.jboss.osgi.spi.SPIMessages.MESSAGES;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.jboss.osgi.spi.util.ServiceLoader;
import org.jboss.osgi.spi.util.StringPropertyReplacer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

/**
 * A simple properties based bootstrap provider
 * 
 * The PropertiesBootstrapProvider supports the following properties
 * 
 * <ul>
 * <li><b>org.jboss.osgi.framework.autoInstall</b> - Bundles that need to be installed with the Framework automatically</li>
 * <li><b>org.jboss.osgi.framework.autoStart</b> - Bundles that need to be started automatically</li>
 * <li><b>org.jboss.osgi.framework.extra</b> - An URL to extra properties, which recursivly may conatin this property.</li>
 * </ul>
 * 
 * All other properties are passed on to configure the framework.
 * 
 * <pre>
 *    # Properties to configure the Framework
 *    org.osgi.framework.storage.clean=onFirstInit
 *    org.osgi.framework.system.packages=\
 *       org.osgi.framework; version=1.4, \
 *       javax.management
 *    
 *    # Bundles that need to be installed with the Framework automatically 
 *    org.jboss.osgi.framework.autoInstall=\
 *       file://${test.archive.directory}/bundles/org.osgi.compendium.jar
 *    
 *    # Bundles that need to be started automatically 
 *    org.jboss.osgi.framework.autoStart=\
 *       file://${test.archive.directory}/bundles/org.apache.felix.log.jar \
 *       file://${test.archive.directory}/bundles/jboss-osgi-common.jar \
 * </pre>
 * 
 * @author thomas.diesler@jboss.com
 * @since 24-Apr-2009
 */
public class PropertiesBootstrapProvider implements OSGiBootstrapProvider {

    /** The default framework property: jboss.osgi.framework.properties */
    public static final String OSGI_FRAMEWORK_CONFIG = "jboss.osgi.framework.properties";
    /** The default framework config: jboss-osgi-framework.properties */
    public static final String DEFAULT_OSGI_FRAMEWORK_PROPERTIES = "jboss-osgi-framework.properties";

    /** Optional list of bundles that get installed automatically: org.jboss.osgi.framework.autoInstall */
    public static final String PROP_OSGI_FRAMEWORK_AUTO_INSTALL = "org.jboss.osgi.framework.autoInstall";
    /** Optional list of bundles that get started automatically: org.jboss.osgi.framework.autoStart */
    public static final String PROP_OSGI_FRAMEWORK_AUTO_START = "org.jboss.osgi.framework.autoStart";
    /** Optional path to extra properties: org.jboss.osgi.framework.extra */
    public static final String PROP_OSGI_FRAMEWORK_EXTRA = "org.jboss.osgi.framework.extra";

    private static Set<String> internalProps = new HashSet<String>();
    static {
        internalProps.add(PROP_OSGI_FRAMEWORK_AUTO_INSTALL);
        internalProps.add(PROP_OSGI_FRAMEWORK_AUTO_START);
        internalProps.add(PROP_OSGI_FRAMEWORK_EXTRA);
    }

    private Framework framework;
    private boolean configured;

    public void configure() {
        configureInternal(System.getProperty(OSGI_FRAMEWORK_CONFIG, DEFAULT_OSGI_FRAMEWORK_PROPERTIES));
    }

    public void configure(String resourceConfig) {
        if (resourceConfig == null)
            throw MESSAGES.illegalArgumentNull("resourceConfig");

        URL urlConfig = Thread.currentThread().getContextClassLoader().getResource(resourceConfig);
        if (urlConfig == null)
            throw MESSAGES.illegalStateCannotFindResource(resourceConfig);

        configure(urlConfig);
    }

    public void configure(URL urlConfig) {
        if (urlConfig == null)
            throw MESSAGES.illegalArgumentNull("config url");

        Map<String, Object> props = getBootstrapProperties(urlConfig);
        initFrameworkInstance(props);
    }

    public void configure(InputStream streamConfig) {
        Map<String, Object> props = getBootstrapProperties(streamConfig);
        initFrameworkInstance(props);
    }

    private void configureInternal(String resourceConfig) {
        if (resourceConfig == null)
            throw MESSAGES.illegalArgumentNull("resourceConfig");

        Map<String, Object> props;
        URL urlConfig = Thread.currentThread().getContextClassLoader().getResource(resourceConfig);
        if (urlConfig != null) {
            props = getBootstrapProperties(urlConfig);
        } else {
            props = new HashMap<String, Object>();
            LOGGER.debugf("Bootstrap using framework defaults");
        }
        initFrameworkInstance(props);
    }

    private void initFrameworkInstance(final Map<String, Object> props) {
        // Load the framework instance
        final Framework frameworkImpl = createFramework(props);
        framework = new GenericFrameworkWrapper<Framework>(frameworkImpl) {

            @Override
            public void start() throws BundleException {
                super.start();

                // Get system bundle context
                BundleContext context = getBundleContext();
                if (context == null)
                    throw MESSAGES.bundleCannotOptainSystemContext();

                // Init the the autoInstall URLs
                List<URL> autoInstall = getBundleURLs(props, PROP_OSGI_FRAMEWORK_AUTO_INSTALL);

                // Init the the autoStart URLs
                List<URL> autoStart = getBundleURLs(props, PROP_OSGI_FRAMEWORK_AUTO_START);

                Map<URL, Bundle> autoBundles = new HashMap<URL, Bundle>();

                // Add the autoStart bundles to autoInstall
                for (URL bundleURL : autoStart) {
                    autoInstall.add(bundleURL);
                }

                // Register system services
                registerSystemServices(context);

                // Install autoInstall bundles
                for (URL bundleURL : autoInstall) {
                    Bundle bundle = context.installBundle(bundleURL.toString());
                    LOGGER.infoBundleInstalled(bundle.getBundleId(), bundle);
                    autoBundles.put(bundleURL, bundle);
                }

                // Start autoStart bundles
                for (URL bundleURL : autoStart) {
                    Bundle bundle = autoBundles.get(bundleURL);
                    if (bundle != null) {
                        bundle.start();
                        LOGGER.infoBundleStarted(bundle.getBundleId(), bundle);
                    }
                }
            }

            @Override
            public void stop() throws BundleException {
                // Unregister system services
                unregisterSystemServices(getBundleContext());
                super.stop();
            }
        };

        configured = true;
    }

    /** Overwrite to create the framework */
    protected Framework createFramework(Map<String, Object> properties) {
        FrameworkFactory factory = ServiceLoader.loadService(FrameworkFactory.class);
        if (factory == null)
            throw MESSAGES.illegalStateCannotLoadService(FrameworkFactory.class.getName());

        Framework framework = factory.newFramework(properties);
        return framework;
    }

    /**
     * Overwrite to register system services before bundles get installed.
     */
    protected void registerSystemServices(BundleContext context) {
        // no default system services
    }

    /**
     * Overwrite to unregister system services before bundles get installed.
     */
    protected void unregisterSystemServices(BundleContext context) {
        // no default system services
    }

    private List<URL> getBundleURLs(Map<String, Object> props, String key) {
        String bundleList = (String) props.get(key);
        if (bundleList == null)
            bundleList = "";

        List<URL> bundleURLs = new ArrayList<URL>();
        for (String bundle : bundleList.split("[, ]")) {
            if (bundle.trim().length() > 0) {
                URL installURL = toURL(bundle);
                bundleURLs.add(installURL);
            }
        }
        return bundleURLs;
    }

    private URL toURL(String path) {
        String realPath = StringPropertyReplacer.replaceProperties(path);
        try {
            URL pathURL = new URL(realPath);
            return pathURL;
        } catch (MalformedURLException ex) {
            throw MESSAGES.illegalStateInvalidPath(ex, path);
        }
    }

    public Framework getFramework() {
        if (configured == false)
            configureInternal(System.getProperty(OSGI_FRAMEWORK_CONFIG, DEFAULT_OSGI_FRAMEWORK_PROPERTIES));

        return framework;
    }

    private Map<String, Object> getBootstrapProperties(URL urlConfig) {
        Map<String, Object> props = null;
        try {
            InputStream propStream = urlConfig.openStream();
            props = getBootstrapProperties(propStream);
            propStream.close();
        } catch (IOException ex) {
            throw MESSAGES.illegalStateCannotConfigureFrom(ex, urlConfig);
        }
        return props;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getBootstrapProperties(InputStream propStream) {
        if (propStream == null)
            throw MESSAGES.illegalArgumentNull("propStream");

        Map<String, Object> propMap = new HashMap<String, Object>();
        try {
            Properties props = new Properties();
            props.load(propStream);
            propStream.close();

            // Process property list
            Enumeration<String> keys = (Enumeration<String>) props.propertyNames();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                String value = props.getProperty(key);

                // Replace property variables
                value = StringPropertyReplacer.replaceProperties(value);
                propMap.put(key, value);

                if (key.endsWith(".instance")) {
                    try {
                        String subkey = key.substring(0, key.lastIndexOf(".instance"));
                        Object instance = Class.forName(value).newInstance();
                        propMap.put(subkey, instance);
                    } catch (Exception ex) {
                        LOGGER.errorCannotLoadPropertyInstance(ex, key, value);
                    }
                }
            }

            // Merge optional extra properties
            String extraPropsValue = (String) propMap.get(PROP_OSGI_FRAMEWORK_EXTRA);
            if (extraPropsValue != null) {
                URL extraPropsURL = null;
                try {
                    extraPropsURL = new URL(extraPropsValue);
                } catch (MalformedURLException e) {
                    // ignore;
                }
                if (extraPropsURL == null) {
                    File propsFile = new File(extraPropsValue);
                    try {
                        extraPropsURL = propsFile.toURI().toURL();
                    } catch (MalformedURLException e) {
                        // ignore;
                    }
                }

                if (extraPropsURL == null)
                    throw MESSAGES.illegalStateInvalidPropertiesURL(extraPropsValue);

                propMap.remove(PROP_OSGI_FRAMEWORK_EXTRA);
                Map<String, Object> extraProps = getBootstrapProperties(extraPropsURL.openStream());
                propMap.putAll(extraProps);
            }
        } catch (IOException ex) {
            throw MESSAGES.illegalStateCannotLoadProperties(ex);
        }
        return propMap;
    }
}
