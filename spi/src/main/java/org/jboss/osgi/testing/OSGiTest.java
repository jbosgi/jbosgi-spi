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
package org.jboss.osgi.testing;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.logging.Logger;
import org.jboss.osgi.vfs.VirtualFile;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.osgi.framework.Bundle;

/**
 * An abstract OSGi Test.
 * 
 * A convenience wrapper for the functionality provided by {@link OSGiTestHelper}.
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 25-Sep-2008
 */
public abstract class OSGiTest {

    // Provide logging
    private static final Logger log = Logger.getLogger(OSGiTest.class);

    /**
     * Writes a a debug start messge
     */
    @Before
    public void setUp() throws Exception {
        log.debug("### START " + getLongName());
    }

    /**
     * Writes a a debug stop messge
     */
    @After
    public void tearDown() throws Exception {
        log.debug("### END " + getLongName());
    }

    /**
     * Get the last token in the FQN of this test class.
     */
    protected String getShortName() {
        String shortName = getClass().getName();
        shortName = shortName.substring(shortName.lastIndexOf(".") + 1);
        return shortName;
    }

    /**
     * Get the the FQN of this test class.
     */
    protected String getLongName() {
        return getClass().getName();
    }

    /**
     * Delegates to {@link OSGiTestHelper#getResourceURL(String)}
     */
    protected URL getResourceURL(String resource) {
        return OSGiTestHelper.getResourceURL(resource);
    }

    /**
     * Delegates to {@link OSGiTestHelper#getResourceFile(String)}
     */
    protected File getResourceFile(String resource) {
        return OSGiTestHelper.getResourceFile(resource);
    }

    /**
     * Delegates to {@link OSGiTestHelper#getTestArchiveURL(String)}
     */
    protected URL getTestArchiveURL(String archive) {
        return OSGiTestHelper.getTestArchiveURL(archive);
    }

    /**
     * Delegates to {@link OSGiTestHelper#getTestArchivePath(String)}
     */
    protected String getTestArchivePath(String archive) {
        return OSGiTestHelper.getTestArchivePath(archive);
    }

    /**
     * Delegates to {@link OSGiTestHelper#getTestArchiveFile(String)}
     */
    protected File getTestArchiveFile(String archive) {
        return OSGiTestHelper.getTestArchiveFile(archive);
    }

    /**
     * Delegates to {@link OSGiTestHelper#getServerHost()}
     */
    protected String getServerHost() {
        return OSGiTestHelper.getServerHost();
    }

    /**
     * Delegates to {@link OSGiTestHelper#getTargetContainer()}
     */
    protected String getTargetContainer() {
        return OSGiTestHelper.getTargetContainer();
    }

    /**
     * Delegates to {@link OSGiTestHelper#getFrameworkName()}
     */
    protected String getFrameworkName() {
        return OSGiTestHelper.getFrameworkName();
    }

    /**
     * Delegates to {@link OSGiTestHelper#assembleArchive(String, String, Class...)}
     */
    protected JavaArchive assembleArchive(String name, String resource, Class<?>... packages) throws Exception {
        return OSGiTestHelper.assembleArchive(name, resource, packages);
    }

    /**
     * Delegates to {@link OSGiTestHelper#assembleArchive(String, String[], Class...)}
     */
    protected JavaArchive assembleArchive(String name, String[] resources, Class<?>... packages) throws Exception {
        return OSGiTestHelper.assembleArchive(name, resources, packages);
    }

    /**
     * Delegates to {@link OSGiTestHelper#toVirtualFile(Archive)}
     */
    protected VirtualFile toVirtualFile(Archive<?> archive) throws IOException, MalformedURLException {
        return OSGiTestHelper.toVirtualFile(archive);
    }

    /**
     * Delegates to {@link OSGiTestHelper#toInputStream(Archive)}
     */
    protected InputStream toInputStream(Archive<?> archive) throws IOException, MalformedURLException {
        return OSGiTestHelper.toInputStream(archive);
    }

    /**
     * Delegates to {@link OSGiTestHelper#assertBundleState(int, int)}
     */
    protected void assertBundleState(int expState, int wasState) {
        OSGiTestHelper.assertBundleState(expState, wasState);
    }

    /**
     * Delegates to {@link OSGiTestHelper#assertLoadClass(Bundle, String)}
     */
    protected Class<?> assertLoadClass(Bundle bundle, String className) {
        return OSGiTestHelper.assertLoadClass(bundle, className);
    }

    /**
     * Delegates to {@link OSGiTestHelper#assertLoadClassFail(Bundle, String)}
     */
    protected void assertLoadClassFail(Bundle bundle, String className) {
        OSGiTestHelper.assertLoadClassFail(bundle, className);
    }

    /**
     * Delegates to {@link OSGiTestHelper#assertLoadClass(Bundle, String, Bundle)}
     */
    protected void assertLoadClass(Bundle bundle, String className, Bundle exporter) {
        OSGiTestHelper.assertLoadClass(bundle, className, exporter);
    }

    boolean isBeforeClassPresent() {
        boolean isPresent = false;
        for (Method method : getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(BeforeClass.class)) {
                isPresent = true;
                break;
            }
        }
        return isPresent;
    }
}
