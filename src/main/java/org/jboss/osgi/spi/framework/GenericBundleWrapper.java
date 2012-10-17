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
package org.jboss.osgi.spi.framework;

import static org.jboss.osgi.spi.SPIMessages.MESSAGES;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;

/**
 * A generic Bundle wrapper that delegates all method calls to the underlying Bundle implementation.
 * 
 * @author thomas.diesler@jboss.com
 * @since 16-Oct-2009
 */
class GenericBundleWrapper<T extends Bundle> implements Bundle {

    private T bundle;

    public GenericBundleWrapper(T bundle) {
        if (bundle == null)
            throw MESSAGES.illegalArgumentNull("bundle");
        this.bundle = bundle;
    }

    protected T getWrappedBundle() {
        return bundle;
    }

    public Enumeration<URL> findEntries(String path, String filePattern, boolean recurse) {
        return bundle.findEntries(path, filePattern, recurse);
    }

    public BundleContext getBundleContext() {
        return bundle.getBundleContext();
    }

    public long getBundleId() {
        return bundle.getBundleId();
    }

    public URL getEntry(String path) {
        return bundle.getEntry(path);
    }

    public Enumeration<String> getEntryPaths(String path) {
        return bundle.getEntryPaths(path);
    }

    public Dictionary<String, String> getHeaders() {
        return bundle.getHeaders();
    }

   public Dictionary<String, String> getHeaders(String locale) {
        return bundle.getHeaders(locale);
    }

    public long getLastModified() {
        return bundle.getLastModified();
    }

    public String getLocation() {
        return bundle.getLocation();
    }

    public ServiceReference<?>[] getRegisteredServices() {
        return bundle.getRegisteredServices();
    }

    public URL getResource(String name) {
        return bundle.getResource(name);
    }

    public Enumeration<URL> getResources(String name) throws IOException {
        return bundle.getResources(name);
    }

    public ServiceReference<?>[] getServicesInUse() {
        return bundle.getServicesInUse();
    }

    public Map<X509Certificate, List<X509Certificate>> getSignerCertificates(int signersType) {
        return bundle.getSignerCertificates(signersType);
    }

    public int getState() {
        return bundle.getState();
    }

    public String getSymbolicName() {
        return bundle.getSymbolicName();
    }

    public Version getVersion() {
        return bundle.getVersion();
    }

    public boolean hasPermission(Object permission) {
        return bundle.hasPermission(permission);
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return bundle.loadClass(name);
    }

    public void start() throws BundleException {
        bundle.start();
    }

    public void start(int options) throws BundleException {
        bundle.start(options);
    }

    public void stop() throws BundleException {
        bundle.stop();
    }

    public void stop(int options) throws BundleException {
        bundle.stop(options);
    }

    public void uninstall() throws BundleException {
        bundle.uninstall();
    }

    public void update() throws BundleException {
        bundle.update();
    }

    public void update(InputStream input) throws BundleException {
        bundle.update(input);
    }

    public int compareTo(Bundle o) {
        return bundle.compareTo(o);
    }

    public <A> A adapt(Class<A> type) {
        return bundle.adapt(type);
    }

    public File getDataFile(String filename) {
        return bundle.getDataFile(filename);
    }

    @Override
    public int hashCode() {
        return bundle.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof GenericBundleWrapper))
            return false;
        GenericBundleWrapper<?> other = (GenericBundleWrapper<?>) obj;
        return bundle.equals(other.bundle);
    }

    @Override
    public String toString() {
        return bundle.toString();
    }
}