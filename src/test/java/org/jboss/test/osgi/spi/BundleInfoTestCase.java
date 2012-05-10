package org.jboss.test.osgi.spi;


import java.util.jar.Manifest;

import junit.framework.Assert;

import org.jboss.osgi.metadata.OSGiMetaData;
import org.jboss.osgi.metadata.OSGiMetaDataBuilder;
import org.jboss.osgi.spi.BundleInfo;
import org.jboss.osgi.spi.ManifestBuilder;
import org.jboss.osgi.spi.OSGiManifestBuilder;
import org.junit.Test;
import org.osgi.framework.BundleException;

/**
 * Test the {@link BundleInfo}.
 *
 * @author thomas.diesler@jboss.com
 * @since 09-May-2012
 */
public class BundleInfoTestCase {

    @Test
    public void testNullManifest() {
        try {
            OSGiManifestBuilder.newInstance().getManifest();
            Assert.fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
            // expected
        }
        try {
            OSGiManifestBuilder.newInstance().openStream();
            Assert.fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
            // expected
        }

        ManifestBuilder builder = ManifestBuilder.newInstance();
        Manifest manifest = builder.getManifest();
        try {
            OSGiManifestBuilder.validateBundleManifest(manifest);
            Assert.fail("BundleException expected");
        } catch (BundleException e) {
            // expected
        }
        Assert.assertFalse("Invalid manifest", OSGiManifestBuilder.isValidBundleManifest(manifest));

        OSGiMetaData metadata = OSGiMetaDataBuilder.load(manifest);
        Assert.assertNotNull("Metadata not null", metadata);
        Assert.assertFalse("Invalid metadata", OSGiMetaDataBuilder.isValidMetadata(metadata));
        try {
            OSGiMetaDataBuilder.validateMetadata(metadata);
            Assert.fail("BundleException expected");
        } catch (BundleException e) {
            // expected
        }
        try {
            metadata.validate();
            Assert.fail("BundleException expected");
        } catch (BundleException e) {
            // expected
        }
    }

    @Test
    public void testR3Manifest() throws BundleException {
        OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
        builder.addBundleName("name");
        Manifest manifest = builder.getManifest();
        Assert.assertTrue("Valid manifest", OSGiManifestBuilder.isValidBundleManifest(manifest));

        OSGiMetaData metadata = OSGiMetaDataBuilder.load(manifest).validate();
        Assert.assertNotNull("Metadata not null", metadata);
        Assert.assertTrue("Valid metadata", OSGiMetaDataBuilder.isValidMetadata(metadata));
        Assert.assertEquals("name", metadata.getBundleName());
        Assert.assertEquals(1, metadata.getBundleManifestVersion());
    }

    @Test
    public void testR4Manifest() throws BundleException {
        OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
        builder.addBundleManifestVersion(2);
        builder.addBundleSymbolicName("name");
        Manifest manifest = builder.getManifest();
        Assert.assertTrue("Valid manifest", OSGiManifestBuilder.isValidBundleManifest(manifest));

        OSGiMetaData metadata = OSGiMetaDataBuilder.load(manifest).validate();
        Assert.assertNotNull("Metadata not null", metadata);
        Assert.assertTrue("Valid metadata", OSGiMetaDataBuilder.isValidMetadata(metadata));
        Assert.assertEquals("name", metadata.getBundleSymbolicName());
        Assert.assertEquals(2, metadata.getBundleManifestVersion());
    }


}
