package org.jboss.test.osgi.spi;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.jar.Manifest;

import org.jboss.osgi.metadata.OSGiMetaData;
import org.jboss.osgi.metadata.OSGiMetaDataBuilder;
import org.jboss.osgi.spi.BundleInfo;
import org.jboss.osgi.spi.OSGiManifestBuilder;
import org.junit.Test;

/**
 * Test the {@link BundleInfo}.
 *
 * @author thomas.diesler@jboss.com
 * @since 09-May-2012
 */
public class BundleInfoTestCase {

    @Test
    public void testNullManifest() {
        OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
        Manifest manifest = builder.getManifest();
        assertFalse("Invalid manifest", BundleInfo.isValidBundleManifest(manifest));

        OSGiMetaData metadata = OSGiMetaDataBuilder.load(manifest);
        assertNotNull("Metadata not null", metadata);
        assertFalse("Invalid metadata", BundleInfo.isValidMetadata(metadata));
    }

    @Test
    public void testR3Manifest() {
        OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
        builder.addBundleName("name");
        Manifest manifest = builder.getManifest();
        assertTrue("Valid manifest", BundleInfo.isValidBundleManifest(manifest));

        OSGiMetaData metadata = OSGiMetaDataBuilder.load(manifest);
        assertNotNull("Metadata not null", metadata);
        assertTrue("Valid metadata", BundleInfo.isValidMetadata(metadata));
        assertEquals("name", metadata.getBundleName());
        assertEquals(1, metadata.getBundleManifestVersion());
    }

    @Test
    public void testR4Manifest() {
        OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
        builder.addBundleManifestVersion(2);
        builder.addBundleSymbolicName("name");
        Manifest manifest = builder.getManifest();
        assertTrue("Valid manifest", BundleInfo.isValidBundleManifest(manifest));

        OSGiMetaData metadata = OSGiMetaDataBuilder.load(manifest);
        assertNotNull("Metadata not null", metadata);
        assertTrue("Valid metadata", BundleInfo.isValidMetadata(metadata));
        assertEquals("name", metadata.getBundleSymbolicName());
        assertEquals(2, metadata.getBundleManifestVersion());
    }


}
