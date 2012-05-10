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

import static org.jboss.osgi.metadata.internal.MetadataMessages.MESSAGES;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.jboss.shrinkwrap.api.asset.Asset;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;

/**
 * A simple OSGi manifest builder.
 *
 * @author thomas.diesler@jboss.com
 * @since 08-Mar-2010
 */
public final class OSGiManifestBuilder extends ManifestBuilder implements Asset {

    private Set<String> importPackages = new LinkedHashSet<String>();
    private Set<String> exportPackages = new LinkedHashSet<String>();
    private Set<String> dynamicImportPackages = new LinkedHashSet<String>();
    private Set<String> requiredBundles = new LinkedHashSet<String>();
    private Set<String> requiredEnvironments = new LinkedHashSet<String>();
    private Manifest manifest;

    public static OSGiManifestBuilder newInstance() {
        return new OSGiManifestBuilder();
    }

    private OSGiManifestBuilder() {
    }

    public OSGiManifestBuilder addBundleManifestVersion(int version) {
        append(Constants.BUNDLE_MANIFESTVERSION + ": " + version);
        return this;
    }

    public OSGiManifestBuilder addBundleSymbolicName(String symbolicName) {
        append(Constants.BUNDLE_SYMBOLICNAME + ": " + symbolicName);
        return this;
    }

    public OSGiManifestBuilder addBundleName(String name) {
        append(Constants.BUNDLE_NAME + ": " + name);
        return this;
    }

    public OSGiManifestBuilder addBundleVersion(Version version) {
        append(Constants.BUNDLE_VERSION + ": " + version);
        return this;
    }

    public OSGiManifestBuilder addBundleVersion(String version) {
        return addBundleVersion(Version.parseVersion(version));
    }

    public OSGiManifestBuilder addBundleActivator(Class<?> bundleActivator) {
        return addBundleActivator(bundleActivator.getName());
    }

    public OSGiManifestBuilder addBundleActivator(String bundleActivator) {
        append(Constants.BUNDLE_ACTIVATOR + ": " + bundleActivator);
        return this;
    }

    public OSGiManifestBuilder addBundleActivationPolicy(String activationPolicy) {
        append(Constants.BUNDLE_ACTIVATIONPOLICY + ": " + activationPolicy);
        return this;
    }

    public OSGiManifestBuilder addFragmentHost(String fragmentHost) {
        append(Constants.FRAGMENT_HOST + ": " + fragmentHost);
        return this;
    }

    public OSGiManifestBuilder addRequireBundle(String requiredBundle) {
        requiredBundles.add(requiredBundle);
        return this;
    }

    public OSGiManifestBuilder addRequireExecutionEnvironment(String... environments) {
        for (String aux : environments) {
            requiredEnvironments.add(aux);
        }
        return this;
    }

    public OSGiManifestBuilder addImportPackages(Class<?>... packages) {
        for (Class<?> aux : packages) {
            importPackages.add(aux.getPackage().getName());
        }
        return this;
    }

    public OSGiManifestBuilder addImportPackages(String... packages) {
        for (String aux : packages) {
            importPackages.add(aux);
        }
        return this;
    }

    public OSGiManifestBuilder addDynamicImportPackages(String... packages) {
        for (String aux : packages) {
            dynamicImportPackages.add(aux);
        }
        return this;
    }

    public OSGiManifestBuilder addExportPackages(Class<?>... packages) {
        for (Class<?> aux : packages) {
            exportPackages.add(aux.getPackage().getName());
        }
        return this;
    }

    public OSGiManifestBuilder addExportPackages(String... packages) {
        for (String aux : packages) {
            exportPackages.add(aux);
        }
        return this;
    }

    public Manifest getManifest() {
        if (manifest == null) {
            // Require-Bundle
            if (requiredBundles.size() > 0) {
                StringBuffer buffer = new StringBuffer();
                buffer.append(Constants.REQUIRE_BUNDLE + ": ");
                Iterator<String> iterator = requiredBundles.iterator();
                buffer.append(iterator.next());
                while (iterator.hasNext()) {
                    buffer.append("," + iterator.next());
                }
                append(buffer.toString());
            }

            // Bundle-RequiredExecutionEnvironment
            if (requiredEnvironments.size() > 0) {
                StringBuffer buffer = new StringBuffer();
                buffer.append(Constants.BUNDLE_REQUIREDEXECUTIONENVIRONMENT + ": ");
                Iterator<String> iterator = requiredEnvironments.iterator();
                buffer.append(iterator.next());
                while (iterator.hasNext()) {
                    buffer.append("," + iterator.next());
                }
                append(buffer.toString());
            }

            // Export-Package
            if (exportPackages.size() > 0) {
                StringBuffer buffer = new StringBuffer();
                buffer.append(Constants.EXPORT_PACKAGE + ": ");
                Iterator<String> iterator = exportPackages.iterator();
                buffer.append(iterator.next());
                while (iterator.hasNext()) {
                    buffer.append("," + iterator.next());
                }
                append(buffer.toString());
            }

            // Import-Package
            if (importPackages.size() > 0) {
                StringBuffer buffer = new StringBuffer();
                buffer.append(Constants.IMPORT_PACKAGE + ": ");
                Iterator<String> iterator = importPackages.iterator();
                buffer.append(iterator.next());
                while (iterator.hasNext()) {
                    buffer.append("," + iterator.next());
                }
                append(buffer.toString());
            }

            // DynamicImport-Package
            if (dynamicImportPackages.size() > 0) {
                StringBuffer buffer = new StringBuffer();
                buffer.append(Constants.DYNAMICIMPORT_PACKAGE + ": ");
                Iterator<String> iterator = dynamicImportPackages.iterator();
                buffer.append(iterator.next());
                while (iterator.hasNext()) {
                    buffer.append("," + iterator.next());
                }
                append(buffer.toString());
            }
            Manifest auxmanifest = super.getManifest();
            try {
                validateBundleManifest(auxmanifest);
            } catch (BundleException ex) {
                throw new IllegalStateException(ex);
            }
            manifest = auxmanifest;
        }
        return manifest;
    }

    /**
     * Validate a given bundle manifest.
     *
     * @param manifest The given manifest
     * @return True if the manifest is valid
     */
    public static boolean isValidBundleManifest(Manifest manifest) {
        if (manifest == null)
            return false;

        try {
            validateBundleManifest(manifest);
            return true;
        } catch (BundleException e) {
            return false;
        }
    }

    /**
     * Validate a given manifest.
     *
     * @param manifest The given manifest
     * @throws BundleException if the given manifest is not a valid OSGi manifest
     */
    public static void validateBundleManifest(Manifest manifest) throws BundleException {
        if (manifest == null)
            MESSAGES.illegalArgumentNull("manifest");

        // A bundle manifest must express the version of the OSGi manifest header
        // syntax in the Bundle-ManifestVersion header. Bundles exploiting this version
        // of the Framework specification (or later) must specify this header.
        // The Framework version 1.3 (or later) bundle manifest version must be ’2’.
        // Bundle manifests written to previous specifications’ manifest syntax are
        // taken to have a bundle manifest version of '1', although there is no way to
        // express this in such manifests.
        int manifestVersion = getBundleManifestVersion(manifest);
        if (manifestVersion < 0)
            throw MESSAGES.bundleCannotObtainBundleManifestVersion();
        if (manifestVersion > 2)
            throw MESSAGES.bundleUnsupportedBundleManifestVersion(manifestVersion);

        String symbolicName = getManifestHeaderInternal(manifest, Constants.BUNDLE_SYMBOLICNAME);
        String bundleVersion = getManifestHeaderInternal(manifest, Constants.BUNDLE_VERSION);

        // R3 Framework
        if (manifestVersion == 1 && symbolicName != null)
            throw MESSAGES.bundleInvalidBundleManifestVersion(symbolicName);

        // R4 Framework
        if (manifestVersion == 2) {
            if (symbolicName == null)
                throw MESSAGES.bundleCannotObtainBundleSymbolicName();

            // Parse the Bundle-Version string
            Version.parseVersion(bundleVersion).toString();
        }
    }

    /**
     * Get the bundle manifest version.
     *
     * @param manifest The given manifest
     * @return The value of the Bundle-ManifestVersion header, or -1 for a non OSGi manifest
     */
    public static int getBundleManifestVersion(Manifest manifest) {
        if (manifest == null)
            throw MESSAGES.illegalArgumentNull("manifest");

        // At least one of these manifest headers must be there
        // Note, in R3 and R4 there is no common mandatory header
        String bundleName = getManifestHeaderInternal(manifest, Constants.BUNDLE_NAME);
        String bundleSymbolicName = getManifestHeaderInternal(manifest, Constants.BUNDLE_SYMBOLICNAME);
        String bundleVersion = getManifestHeaderInternal(manifest, Constants.BUNDLE_VERSION);

        if (bundleName == null && bundleSymbolicName == null && bundleVersion == null)
            return -1;

        String manifestVersion = getManifestHeaderInternal(manifest, Constants.BUNDLE_MANIFESTVERSION);
        return manifestVersion != null ? Integer.parseInt(manifestVersion) : 1;
    }

    private static String getManifestHeaderInternal(Manifest manifest, String key) {
        Attributes attribs = manifest.getMainAttributes();
        String value = attribs.getValue(key);
        return value != null ? value.trim() : null;
    }
}
