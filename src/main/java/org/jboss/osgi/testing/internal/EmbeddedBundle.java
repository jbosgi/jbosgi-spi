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
package org.jboss.osgi.testing.internal;

import java.io.File;
import java.net.URL;
import java.util.Dictionary;

import org.jboss.osgi.testing.OSGiBundle;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * An OSGi Test Case
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 25-Sep-2008
 */
public class EmbeddedBundle extends OSGiBundleImpl {

    private Bundle bundle;

    public EmbeddedBundle(OSGiRuntimeImpl runtime, Bundle bundle) {
        super(runtime);
        this.bundle = bundle;
    }

    public Bundle getBundle() {
        return bundle;
    }

    @Override
    public int getState() {
        return bundle.getState();
    }

    @Override
    public String getSymbolicName() {
        return bundle.getSymbolicName();
    }

    @Override
    public Version getVersion() {
        return bundle.getVersion();
    }

    @Override
    public String getLocation() {
        return bundle.getLocation();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Dictionary<String, String> getHeaders() {
        return bundle.getHeaders();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Dictionary<String, String> getHeaders(String locale) {
        return bundle.getHeaders(locale);
    }

    @Override
    public long getBundleId() {
        return bundle.getBundleId();
    }

    @Override
    public String getProperty(String key) {
        return bundle.getBundleContext().getProperty(key);
    }

    @Override
    public URL getEntry(String path) {
        return bundle.getEntry(path);
    }

    @Override
    public URL getResource(String name) {
        return bundle.getResource(name);
    }

    @Override
    public File getDataFile(String filename) {
        return bundle.getBundleContext().getDataFile(filename);
    }

    @Override
    public OSGiBundle loadClass(String name) throws ClassNotFoundException {
        Class<?> clazz = bundle.loadClass(name);
        Bundle providerBundle = getPackageAdmin().getBundle(clazz);
        if (providerBundle == null)
            return null;

        return getRuntime().getBundle(providerBundle.getBundleId());
    }

    @Override
    protected void startInternal() throws BundleException {
        bundle.start();
    }

    @Override
    protected void stopInternal() throws BundleException {
        bundle.stop();
    }

    @Override
    protected void uninstallInternal() throws BundleException {
        assertNotUninstalled();
        bundle.uninstall();
        OSGiRuntimeImpl runtimeImpl = (OSGiRuntimeImpl) getRuntime();
        runtimeImpl.unregisterBundle(this);
    }

    private PackageAdmin getPackageAdmin() {
        BundleContext context = ((EmbeddedRuntimeImpl) getRuntime()).getSystemContext();
        ServiceReference sref = context.getServiceReference(PackageAdmin.class.getName());
        return (PackageAdmin) context.getService(sref);
    }
}
