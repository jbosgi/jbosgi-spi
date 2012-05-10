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

import org.jboss.osgi.spi.ManifestBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.Constants;

import java.io.IOException;
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
    public void testLongLine() throws IOException {
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
