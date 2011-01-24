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
package org.jboss.osgi.testing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;

/**
 * A simple OSGi manifest builder.
 * 
 * @author thomas.diesler@jboss.com
 * @since 08-Mar-2010
 */
public final class OSGiManifestBuilder implements Asset {

    // Provide logging
    private static final Logger log = Logger.getLogger(OSGiManifestBuilder.class);

    private StringWriter sw;
    private PrintWriter pw;
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
        sw = new StringWriter();
        pw = new PrintWriter(sw);
        append(Attributes.Name.MANIFEST_VERSION + ": 1.0", true);
    }

    public OSGiManifestBuilder addBundleManifestVersion(int version) {
        append(Constants.BUNDLE_MANIFESTVERSION + ": " + version, true);
        return this;
    }

    public OSGiManifestBuilder addBundleSymbolicName(String symbolicName) {
        append(Constants.BUNDLE_SYMBOLICNAME + ": " + symbolicName, true);
        return this;
    }

    public OSGiManifestBuilder addBundleName(String name) {
        append(Constants.BUNDLE_NAME + ": " + name, true);
        return this;
    }

    public OSGiManifestBuilder addBundleVersion(Version version) {
        append(Constants.BUNDLE_VERSION + ": " + version, true);
        return this;
    }

    public OSGiManifestBuilder addBundleVersion(String version) {
        return addBundleVersion(Version.parseVersion(version));
    }

    public OSGiManifestBuilder addBundleActivator(Class<?> bundleActivator) {
        return addBundleActivator(bundleActivator.getName());
    }

    public OSGiManifestBuilder addBundleActivator(String bundleActivator) {
        append(Constants.BUNDLE_ACTIVATOR + ": " + bundleActivator, true);
        return this;
    }

    public OSGiManifestBuilder addBundleActivationPolicy(String activationPolicy) {
        append(Constants.BUNDLE_ACTIVATIONPOLICY + ": " + activationPolicy, true);
        return this;
    }

    public OSGiManifestBuilder addFragmentHost(String fragmentHost) {
        append(Constants.FRAGMENT_HOST + ": " + fragmentHost, true);
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

    public OSGiManifestBuilder addManifestHeader(String key, String value) {
        append(key + ": " + value, true);
        return this;
    }

    public Manifest getManifest() {
        if (manifest == null) {
            // Require-Bundle
            if (requiredBundles.size() > 0) {
                append(Constants.REQUIRE_BUNDLE + ": ", false);
                Iterator<String> iterator = requiredBundles.iterator();
                append(iterator.next(), false);
                while (iterator.hasNext())
                    append("," + iterator.next(), false);
                append(null, true);
            }

            // Bundle-RequiredExecutionEnvironment
            if (requiredEnvironments.size() > 0) {
                append(Constants.BUNDLE_REQUIREDEXECUTIONENVIRONMENT + ": ", false);
                Iterator<String> iterator = requiredEnvironments.iterator();
                append(iterator.next(), false);
                while (iterator.hasNext())
                    append("," + iterator.next(), false);
                append(null, true);
            }

            // Export-Package
            if (exportPackages.size() > 0) {
                append(Constants.EXPORT_PACKAGE + ": ", false);
                Iterator<String> iterator = exportPackages.iterator();
                append(iterator.next(), false);
                while (iterator.hasNext())
                    append("," + iterator.next(), false);
                append(null, true);
            }

            // Import-Package
            if (importPackages.size() > 0) {
                append(Constants.IMPORT_PACKAGE + ": ", false);
                Iterator<String> iterator = importPackages.iterator();
                append(iterator.next(), false);
                while (iterator.hasNext())
                    append("," + iterator.next(), false);
                append(null, true);
            }

            // DynamicImport-Package
            if (dynamicImportPackages.size() > 0) {
                append(Constants.DYNAMICIMPORT_PACKAGE + ": ", false);
                Iterator<String> iterator = dynamicImportPackages.iterator();
                append(iterator.next(), false);
                while (iterator.hasNext())
                    append("," + iterator.next(), false);
                append(null, true);
            }

            String manifestString = sw.toString();
            if (log.isTraceEnabled())
                log.trace(manifestString);

            try {
                manifest = new Manifest(new ByteArrayInputStream(manifestString.getBytes()));
            } catch (IOException ex) {
                throw new IllegalStateException("Cannot create manifest", ex);
            }
        }
        return manifest;
    }

    @Override
    public InputStream openStream() {
        Manifest manifest = getManifest();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            manifest.write(baos);
            return new ByteArrayInputStream(baos.toByteArray());
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot provide manifest InputStream", ex);
        }
    }

    private void append(String line, boolean newline) {
        if (manifest != null)
            throw new IllegalStateException("Cannot append to already existing manifest");

        if (line != null)
            pw.print(line);
        if (newline == true)
            pw.println();
    }
}
