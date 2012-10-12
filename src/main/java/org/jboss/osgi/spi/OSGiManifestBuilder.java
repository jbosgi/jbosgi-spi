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
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
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
package org.jboss.osgi.spi;

import java.io.InputStream;
import java.util.jar.Manifest;

import org.jboss.shrinkwrap.api.asset.Asset;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

/**
 * A simple OSGi manifest builder.
 *
 * @author thomas.diesler@jboss.com
 * @since 08-Mar-2010
 * @deprecated
 */
public final class OSGiManifestBuilder extends ManifestBuilder implements Asset {

    private final org.jboss.osgi.metadata.OSGiManifestBuilder delegate = org.jboss.osgi.metadata.OSGiManifestBuilder.newInstance();

    public static OSGiManifestBuilder newInstance() {
        return new OSGiManifestBuilder();
    }

    private OSGiManifestBuilder() {
    }

    public static boolean isValidBundleManifest(Manifest manifest) {
        return org.jboss.osgi.metadata.OSGiManifestBuilder.isValidBundleManifest(manifest);
    }

    public static void validateBundleManifest(Manifest manifest) throws BundleException {
        org.jboss.osgi.metadata.OSGiManifestBuilder.validateBundleManifest(manifest);
    }

    public static int getBundleManifestVersion(Manifest manifest) {
        return org.jboss.osgi.metadata.OSGiManifestBuilder.getBundleManifestVersion(manifest);
    }

    public OSGiManifestBuilder addBundleManifestVersion(int version) {
        delegate.addBundleManifestVersion(version);
        return this;
    }

    public OSGiManifestBuilder addBundleSymbolicName(String symbolicName) {
        delegate.addBundleSymbolicName(symbolicName);
        return this;
    }

    public OSGiManifestBuilder addBundleName(String name) {
        delegate.addBundleName(name);
        return this;
    }

    public OSGiManifestBuilder addBundleVersion(Version version) {
        delegate.addBundleVersion(version);
        return this;
    }

    public OSGiManifestBuilder addBundleVersion(String version) {
        delegate.addBundleVersion(version);
        return this;
    }

    public OSGiManifestBuilder addBundleActivator(Class<?> bundleActivator) {
        delegate.addBundleActivator(bundleActivator);
        return this;
    }

    public OSGiManifestBuilder addBundleActivator(String bundleActivator) {
        delegate.addBundleActivator(bundleActivator);
        return this;
    }

    public OSGiManifestBuilder addBundleActivationPolicy(String activationPolicy) {
        delegate.addBundleActivationPolicy(activationPolicy);
        return this;
    }

    public OSGiManifestBuilder addBundleClasspath(String classpath) {
        delegate.addBundleClasspath(classpath);
        return this;
    }

    public OSGiManifestBuilder addFragmentHost(String fragmentHost) {
        delegate.addFragmentHost(fragmentHost);
        return this;
    }

    public OSGiManifestBuilder addRequireBundle(String requiredBundle) {
        delegate.addRequireBundle(requiredBundle);
        return this;
    }

    public OSGiManifestBuilder addRequireExecutionEnvironment(String... environments) {
        delegate.addRequireExecutionEnvironment(environments);
        return this;
    }

    public OSGiManifestBuilder addImportPackages(Class<?>... packages) {
        delegate.addImportPackages(packages);
        return this;
    }

    public OSGiManifestBuilder addImportPackages(String... packages) {
        delegate.addImportPackages(packages);
        return this;
    }

    public OSGiManifestBuilder addDynamicImportPackages(String... packages) {
        delegate.addDynamicImportPackages(packages);
        return this;
    }

    public OSGiManifestBuilder addExportPackages(Class<?>... packages) {
        delegate.addExportPackages(packages);
        return this;
    }

    public OSGiManifestBuilder addExportPackages(String... packages) {
        delegate.addExportPackages(packages);
        return this;
    }

    public OSGiManifestBuilder addProvidedCapabilities(String... capabilities) {
        delegate.addProvidedCapabilities(capabilities);
        return this;
    }

    public OSGiManifestBuilder addRequiredCapabilities(String... capabilities) {
        delegate.addRequiredCapabilities(capabilities);
        return this;
    }

    public ManifestBuilder addManifestHeader(String key, String value) {
        delegate.addManifestHeader(key, value);
        return this;
    }

    public Manifest getManifest() {
        return delegate.getManifest();
    }

    public InputStream openStream() {
        return delegate.openStream();
    }
}
