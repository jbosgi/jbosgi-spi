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
package org.jboss.osgi.spi.capability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.logging.Logger;
import org.jboss.osgi.spi.util.BundleInfo;
import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiRuntime;
import org.jboss.osgi.testing.OSGiRuntimeHelper;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

/**
 * An abstract OSGi capability that can be installed in an OSGiRuntime.
 * 
 * The capability is only installed if the service name given in the constructor is not already registered with the OSGi
 * framework.
 * 
 * It maintains an ordered set of dependent capabilities and bundles that must be installed to provide the functionality
 * advertised by this capability.
 * 
 * @author thomas.diesler@jboss.com
 * @since 05-May-2009
 */
public abstract class Capability {

    // Provide logging
    private static final Logger log = Logger.getLogger(Capability.class);

    private String serviceName;
    private String filter;
    private Map<String, String> systemProperties;

    private List<Capability> dependencies;
    private List<BundleInfo> bundles;
    private List<OSGiBundle> installed = new ArrayList<OSGiBundle>();

    /**
     * Construct a capability that is identified by the given service name.
     * 
     * If the service name is already registered with the OSGiRuntime adding this capability does nothing.
     * 
     * If the service name is null the capability will install each associated bundle unless a bundle with the same symbolic
     * name is already installed.
     * 
     * @param serviceName The service that would be registered by this capability.
     */
    public Capability(String serviceName) {
        this(serviceName, null);
    }

    /**
     * Construct a capability that is identified by the given service name and filter string.
     * 
     * If the service is already registered with the OSGiRuntime adding this capability does nothing.
     */
    public Capability(String serviceName, String filter) {
        this.serviceName = serviceName;
        this.filter = filter;
    }

    /**
     * Get the service name associated with this capability.
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Get the filter that is used for service lookup.
     */
    public String getFilter() {
        return filter;
    }

    /**
     * Set the filter that is used for service lookup.
     */
    public void setFilter(String filter) {
        this.filter = filter;
    }

    /**
     * Add a system property provided by this capability.
     * 
     * Adding this capability will set the associated system properties if a propperty is not set already.
     */
    public void addSystemProperty(String key, String value) {
        getPropertiesInternal().put(key, value);
    }

    /**
     * Get the system properties for this capability.
     */
    public Map<String, String> getSystemProperties() {
        return Collections.unmodifiableMap(getPropertiesInternal());
    }

    public List<Capability> getDependencies() {
        return Collections.unmodifiableList(getDependenciesInternal());
    }

    protected void addDependency(Capability dependency) {
        getDependenciesInternal().add(dependency);
    }

    public List<BundleInfo> getBundles() {
        return Collections.unmodifiableList(getBundlesInternal());
    }

    protected void addBundle(String location) {
        BundleInfo info;
        try {
            info = BundleInfo.createBundleInfo(location);
        } catch (BundleException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException)
                throw (RuntimeException) cause;

            throw new IllegalArgumentException("Cannot create bundle info for: " + location, ex);
        }
        getBundlesInternal().add(info);
    }

    private Map<String, String> getPropertiesInternal() {
        if (systemProperties == null)
            systemProperties = new HashMap<String, String>();

        return systemProperties;
    }

    private List<Capability> getDependenciesInternal() {
        if (dependencies == null)
            dependencies = new ArrayList<Capability>();

        return dependencies;
    }

    private List<BundleInfo> getBundlesInternal() {
        if (bundles == null)
            bundles = new ArrayList<BundleInfo>();

        return bundles;
    }

    public List<OSGiBundle> getInstalledBundles() {
        return Collections.unmodifiableList(installed);
    }

    public void install(OSGiRuntime runtime) throws BundleException {
        log.debug("Install capability: " + this);
        for (BundleInfo info : getBundles()) {
            String location = info.getLocation();
            String symName = info.getSymbolicName();
            Version version = info.getVersion();
            if (runtime.getBundle(symName, version) == null) {
                OSGiBundle bundle = runtime.installBundle(location);
                installed.add(bundle);
            } else {
                log.debug("Skip bundle: " + location);
            }
        }
    }

    public void start(OSGiRuntime runtime) throws BundleException {
        log.debug("Start capability: " + this);
        for (OSGiBundle bundle : getInstalledBundles()) {
            bundle.start();
        }
    }

    public void stop(OSGiRuntime runtime) {
        log.debug("Stop capability: " + this);
        List<OSGiBundle> installedReverse = new ArrayList<OSGiBundle>(getInstalledBundles());
        Collections.reverse(installedReverse);

        for (OSGiBundle bundle : installedReverse) {
            if (bundle.getState() != Bundle.UNINSTALLED)
                OSGiRuntimeHelper.failsafeStop(bundle);
        }
    }

    public void uninstall(OSGiRuntime runtime) {
        log.debug("Uninstall capability: " + this);
        List<OSGiBundle> installedReverse = new ArrayList<OSGiBundle>(getInstalledBundles());
        Collections.reverse(installedReverse);

        for (OSGiBundle bundle : installedReverse) {
            if (bundle.getState() != Bundle.UNINSTALLED)
                OSGiRuntimeHelper.failsafeUninstall(bundle);
            installed.remove(bundle);
        }
    }
}