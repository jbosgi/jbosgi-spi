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
package org.jboss.osgi.spi;

import static org.jboss.osgi.spi.internal.SPIMessages.MESSAGES;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.jboss.osgi.metadata.OSGiMetaData;
import org.jboss.osgi.metadata.OSGiMetaDataBuilder;
import org.jboss.osgi.vfs.AbstractVFS;
import org.jboss.osgi.vfs.VFSUtils;
import org.jboss.osgi.vfs.VirtualFile;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;

/**
 * Primitive access to bundle meta data and root virtual file.
 *
 * The bundle info can be constructed from various locations.
 * If that succeeds, there is a valid OSGi Manifest.
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Oct-2009
 */
public class BundleInfo implements Serializable {

    private static final long serialVersionUID = -2363297020450715134L;

    private final URL rootURL;
    private final String location;
    private final String symbolicName;
    private final Version bundleVersion;
    private final OSGiMetaData metadata;

    private transient VirtualFile rootFile;
    private transient Manifest manifest;

    public static BundleInfo createBundleInfo(String location) throws BundleException {
        if (location == null)
            throw MESSAGES.illegalArgumentNull("location");

        URL url = getRealLocation(location);
        if (url == null)
            throw MESSAGES.illegalArgumentCannotObtainRealLocation(location);

        return new BundleInfo(toVirtualFile(url), url.toExternalForm(), null);
    }

    public static BundleInfo createBundleInfo(URL url) throws BundleException {
        if (url == null)
            throw MESSAGES.illegalArgumentNull("url");

        return new BundleInfo(toVirtualFile(url), url.toExternalForm(), null);
    }

    public static BundleInfo createBundleInfo(VirtualFile root) throws BundleException {
        return new BundleInfo(root, null, null);
    }

    public static BundleInfo createBundleInfo(VirtualFile root, String location) throws BundleException {
        return new BundleInfo(root, location, null);
    }

    public static BundleInfo createBundleInfo(VirtualFile root, String location, OSGiMetaData metadata) throws BundleException {
        return new BundleInfo(root, location, metadata);
    }

    private BundleInfo(VirtualFile rootFile, String location, OSGiMetaData metadata) throws BundleException {
        if (rootFile == null)
            throw MESSAGES.illegalArgumentNull("rootFile");

        this.rootFile = rootFile;
        this.rootURL = toURL(rootFile);

        // Derive the location from the root
        if (location == null) {
            location = rootURL.toExternalForm();
        }
        this.location = location;

        // Initialize the metadata
        if (metadata == null) {
            try {
                manifest = VFSUtils.getManifest(rootFile);
                if (manifest == null) {
                    throw MESSAGES.bundleCannotGetManifest(null, rootURL);
                }
            } catch (IOException ex) {
                throw MESSAGES.bundleCannotGetManifest(ex, rootURL);
            }
            metadata = OSGiMetaDataBuilder.load(manifest);
        }
        OSGiMetaDataBuilder.validateMetadata(metadata);
        this.metadata = metadata;

        symbolicName = metadata.getBundleSymbolicName();
        bundleVersion = metadata.getBundleVersion();
    }

    /**
     * Validate manifest from the given virtual file.
     *
     * @param virtualFile The virtualFile that is checked for a valid manifest
     * @return True if the virtualFile conatains a valid manifest
     */
    public static boolean isValidBundle(VirtualFile virtualFile) {
        try {
            Manifest manifest = VFSUtils.getManifest(virtualFile);
            return isValidBundleManifest(manifest);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Validate a given bundle manifest.
     *
     * @param manifest The given manifest
     * @return True if the manifest is valid
     * @deprecated use {@link OSGiManifestBuilder#isValidBundleManifest(Manifest)}
     */
    public static boolean isValidBundleManifest(Manifest manifest) {
        return OSGiManifestBuilder.isValidBundleManifest(manifest);
    }

    /**
     * Validate a given bundle manifest.
     *
     * @param manifest The given manifest
     * @throws BundleException if the given manifest is not a valid
     * @deprecated use {@link OSGiManifestBuilder#validateBundleManifest(Manifest)}
     */
    public static void validateBundleManifest(Manifest manifest) throws BundleException {
        OSGiManifestBuilder.validateBundleManifest(manifest);
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

    /**
     * Get the manifest header for the given key.
     */
    public String getManifestHeader(String key) {
        String value = getManifestHeaderInternal(getManifest(), key);
        return value;
    }

    /**
     * Get the bundle location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Get the bundle root file
     */
    public VirtualFile getRoot() {
        if (rootFile == null)
            rootFile = toVirtualFile(rootURL);

        return rootFile;
    }

    /**
     * Get the bundle root url
     */
    public URL getRootURL() {
        return toURL(getRoot());
    }

    /**
     * Get the bundle symbolic name
     */
    public String getSymbolicName() {
        return symbolicName;
    }

    /**
     * Get the bundle version
     */
    public Version getVersion() {
        return bundleVersion;
    }

    /**
     * Get the OSGi metadata
     */
    public OSGiMetaData getOSGiMetadata() {
        return metadata;
    }

    /**
     * Closes the accociated resources.
     */
    public void close() {
        if (rootFile != null)
            VFSUtils.safeClose(rootFile);
    }

    public Manifest getManifest() {
        if (manifest == null) {
            try {
                manifest = VFSUtils.getManifest(getRoot());
            } catch (Exception ex) {
                throw MESSAGES.illegalStateCannotGetManifest(ex, rootURL);
            }
        }
        return manifest;
    }

    private static VirtualFile toVirtualFile(URL url) {
        try {
            return AbstractVFS.toVirtualFile(url);
        } catch (IOException ex) {
            throw MESSAGES.illegalArgumentInvalidRootURL(ex, url);
        }
    }

    private static URL getRealLocation(String location) {
        // Try location as URL
        URL url = null;
        try {
            url = new URL(location);
        } catch (MalformedURLException ex) {
            // ignore
        }

        // Try location as File
        if (url == null) {
            try {
                File file = new File(location);
                if (file.exists())
                    url = file.toURI().toURL();
            } catch (MalformedURLException e) {
                // ignore
            }
        }

        // Try to prefix the location with the test archive directory
        if (url == null) {
            String prefix = System.getProperty("test.archive.directory", "target/test-libs");
            if (location.startsWith(prefix) == false && new File(prefix).exists())
                return getRealLocation(prefix + File.separator + location);
        }

        return url;
    }

    private static URL toURL(VirtualFile file) {
        try {
            return file.toURL();
        } catch (Exception ex) {
            throw MESSAGES.illegalArgumentInvalidRootFile(ex, file);
        }
    }

    private String toEqualString() {
        return "[" + symbolicName + ":" + bundleVersion + ",url=" + rootURL + "]";
    }

    private static String getManifestHeaderInternal(Manifest manifest, String key) {
        Attributes attribs = manifest.getMainAttributes();
        String value = attribs.getValue(key);
        return value != null ? value.trim() : null;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BundleInfo))
            return false;

        BundleInfo other = (BundleInfo) obj;
        return toEqualString().equals(other.toEqualString());
    }

    @Override
    public int hashCode() {
        return toEqualString().hashCode();
    }

    @Override
    public String toString() {
        return toEqualString();
    }
}