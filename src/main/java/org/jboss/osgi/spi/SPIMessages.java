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
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

import java.net.URL;

import org.jboss.logging.Messages;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;
import org.jboss.osgi.vfs.VirtualFile;
import org.osgi.framework.BundleException;

/**
 * Logging Id ranges: 10300-10399
 *
 * https://docs.jboss.org/author/display/JBOSGI/JBossOSGi+Logging
 *
 * @author Thomas.Diesler@jboss.com
 */
@MessageBundle(projectCode = "JBOSGI")
public interface SPIMessages {

    SPIMessages MESSAGES = Messages.getBundle(SPIMessages.class);

    @Message(id = 10300, value = "%s is null")
    IllegalArgumentException illegalArgumentNull(String name);

    @Message(id = 10301, value = "Cannot find class '%s' in: %s")
    IllegalArgumentException illegalArgumentCannotFindClassInKey(String clazz, String key);

    @Message(id = 10302, value = "Cannot obtain real location for: %s")
    IllegalArgumentException illegalArgumentCannotObtainRealLocation(String location);

    @Message(id = 10303, value = "Invalid root url: %s")
    IllegalArgumentException illegalArgumentInvalidRootURL(@Cause Throwable cause, URL url);

    @Message(id = 10304, value = "Invalid root file: %s")
    IllegalArgumentException illegalArgumentInvalidRootFile(@Cause Throwable cause, VirtualFile file);

    @Message(id = 10305, value = "Cannot find resource: %s")
    IllegalStateException illegalStateCannotFindResource(String resourceConfig);

    @Message(id = 10306, value = "Cannot load service: META-INF/services/%s")
    IllegalStateException illegalStateCannotLoadService(String className);

    @Message(id = 10307, value = "Invalid path: %s")
    IllegalStateException illegalStateInvalidPath(@Cause Throwable cause, String path);

    @Message(id = 10308, value = "Cannot configure from: %s")
    IllegalStateException illegalStateCannotConfigureFrom(@Cause Throwable cause, URL urlConfig);

    @Message(id = 10309, value = "Invalid properties URL: %s")
    IllegalStateException illegalStateInvalidPropertiesURL(String urlSpec);

    @Message(id = 10310, value = "Cannot load properties")
    IllegalStateException illegalStateCannotLoadProperties(@Cause Throwable cause);

    //@Message(id = 10311, value = "Cannot start framework")
    //IllegalStateException illegalStateCannotStartFramework(@Cause Throwable cause);

    @Message(id = 10312, value = "Cannot load service: %s")
    IllegalStateException illegalStateCannotLoadServiceClass(@Cause Throwable cause, String serviceClass);

    @Message(id = 10313, value = "Cannot get manifest from: %s")
    IllegalStateException illegalStateCannotGetManifest(@Cause Throwable cause, URL rootURL);

    //@Message(id = 10314, value = "Cannot create manifest")
    //IllegalStateException illegalStateCannotCreateManifest(@Cause Throwable cause);

    //@Message(id = 10315, value = "Cannot provide manifest input stream")
    //IllegalStateException illegalStateCannotProvideManifestInputStream(@Cause Throwable cause);

    //@Message(id = 10316, value = "Cannot append to already existing manifest")
    //IllegalStateException illegalStateCannotAppendToExistingManifest();

    @Message(id = 10317, value = "Cannot obtain system context")
    BundleException bundleCannotOptainSystemContext();

    @Message(id = 10318, value = "Cannot get manifest from: %s")
    BundleException bundleCannotGetManifest(@Cause Throwable cause, URL rootURL);

    //@Message(id = 10319, value = "Cannot obtain Bundle-ManifestVersion")
    //BundleException bundleCannotObtainBundleManifestVersion();

    //@Message(id = 10320, value = "Unsupported Bundle-ManifestVersion: %d")
    //BundleException bundleUnsupportedBundleManifestVersion(int version);

    //@Message(id = 10321, value = "Invalid Bundle-ManifestVersion for: %s")
    //BundleException bundleInvalidBundleManifestVersion(String symbolicName);

    //@Message(id = 10322, value = "Cannot obtain Bundle-SymbolicName")
    //BundleException bundleCannotObtainBundleSymbolicName();
}
