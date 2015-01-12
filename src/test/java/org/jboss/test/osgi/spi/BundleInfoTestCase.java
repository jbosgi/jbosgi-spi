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
package org.jboss.test.osgi.spi;


import java.util.jar.Manifest;

import org.junit.Assert;

import org.jboss.osgi.metadata.ManifestBuilder;
import org.jboss.osgi.metadata.OSGiManifestBuilder;
import org.jboss.osgi.metadata.OSGiMetaData;
import org.jboss.osgi.metadata.OSGiMetaDataBuilder;
import org.jboss.osgi.spi.BundleInfo;
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

        /*
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
        */
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
