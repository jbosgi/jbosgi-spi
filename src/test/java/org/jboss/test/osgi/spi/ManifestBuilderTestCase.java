package org.jboss.test.osgi.spi;

import org.jboss.osgi.testing.ManifestBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.Constants;

import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Test the simple manifest builder.
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Jan-2012
 */
public class ManifestBuilderTestCase {

    @Test
    public void testLongLine() {
        String importPackages = "org.jboss.osgi.deployment.interceptor,org.osgi.service.packageadmin,org.osgi.service.http,javax.servlet.http,javax.servlet,org.jboss.osgi.resolver.v2,org.osgi.service.repository,org.osgi.framework.resource,org.junit.runner,org.osgi.framework,org.jboss.shrinkwrap.api.spec,org.jboss.arquillian.container.test.api,org.jboss.arquillian.junit,org.jboss.arquillian.osgi,org.jboss.arquillian.test.api,org.jboss.osgi.testing,org.jboss.shrinkwrap.api,org.jboss.shrinkwrap.api.asset,org.junit,javax.inject";

        ManifestBuilder builder = ManifestBuilder.newInstance();
        builder.addManifestHeader(Constants.BUNDLE_MANIFESTVERSION, "2");
        builder.addManifestHeader(Constants.BUNDLE_SYMBOLICNAME, "example-webapp-negative");
        builder.addManifestHeader(Constants.EXPORT_PACKAGE, "org.jboss.test.osgi.example.webapp");
        builder.addManifestHeader(Constants.IMPORT_PACKAGE, importPackages);
        Manifest manifest = builder.getManifest();
        Assert.assertNotNull("Manifest not null", manifest);

        Attributes attributes = manifest.getMainAttributes();
        String value = attributes.getValue(Constants.EXPORT_PACKAGE);
        Assert.assertEquals("org.jboss.test.osgi.example.webapp", value);
        value = attributes.getValue(Constants.IMPORT_PACKAGE);
        Assert.assertEquals(importPackages, value);
    }
}
