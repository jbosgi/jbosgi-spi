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
package org.jboss.osgi.spi.util;

import static org.jboss.osgi.spi.SPIMessages.MESSAGES;

import java.io.File;

import org.osgi.framework.BundleContext;

/**
 * A utility class for replacing properties in strings.
 * 
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="Scott.Stark@jboss.org">Scott Stark</a>
 * @author <a href="claudio.vesco@previnet.it">Claudio Vesco</a>
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 16-Sep-2010
 */
public final class StringPropertyReplacer {

    /** New line string constant */
    public static final String NEWLINE = SecurityActions.getSystemProperty("line.separator", "\n");

    /** File separator value */
    private static final String FILE_SEPARATOR = File.separator;

    /** Path separator value */
    private static final String PATH_SEPARATOR = File.pathSeparator;

    /** File separator alias */
    private static final String FILE_SEPARATOR_ALIAS = "/";

    /** Path separator alias */
    private static final String PATH_SEPARATOR_ALIAS = ":";

    // States used in property parsing
    private static final int NORMAL = 0;
    private static final int SEEN_DOLLAR = 1;
    private static final int IN_BRACKET = 2;

    public interface PropertyProvider {

        String getProperty(String key);
    }

    /**
     * Go through the input string and replace any occurance of ${p} with the System.getProperty(p) value. If there is no such
     * property p defined, then the ${p} reference will remain unchanged.
     * 
     * @param string - the string with possible ${} references
     * @return the input string with all property references replaced if any. If there are no valid references the input string
     *         will be returned.
     */
    public static String replaceProperties(final String string) {
        return replaceProperties(string, new PropertyProvider() {

            @Override
            public String getProperty(String key) {
                return System.getProperty(key);
            }
        });
    }

    /**
     * Go through the input string and replace any occurance of ${p} with the BundleContext.getProperty(p) value. If there is no
     * such property p defined, then the ${p} reference will remain unchanged.
     * 
     * @param string - the string with possible ${} references
     * @return the input string with all property references replaced if any. If there are no valid references the input string
     *         will be returned.
     */
    public static String replaceProperties(final String string, final BundleContext context) {
        return replaceProperties(string, new PropertyProvider() {

            @Override
            public String getProperty(String key) {
                return context.getProperty(key);
            }
        });
    }

    /**
     * Go through the input string and replace any occurance of ${p} with the PropertyProvider.getProperty(p) value. If there is
     * no such property p defined, then the ${p} reference will remain unchanged.
     * 
     * If the property reference is of the form ${p:v} and there is no such property p, then the default value v will be
     * returned.
     * 
     * If the property reference is of the form ${p1,p2} or ${p1,p2:v} then the primary and the secondary properties will be
     * tried in turn, before returning either the unchanged input, or the default value.
     * 
     * The property ${/} is replaced with PropertyProvider.getProperty("file.separator") value and the property ${:} is replaced
     * with PropertyProvider.getProperty("path.separator").
     * 
     * @param string - the string with possible ${} references
     * @param provider - the source for ${x} property ref values
     * @return the input string with all property references replaced if any. If there are no valid references the input string
     *         will be returned.
     */
    public static String replaceProperties(final String string, final PropertyProvider provider) {
        if (string == null)
            throw MESSAGES.illegalArgumentNull("string");
        if (provider == null)
            throw MESSAGES.illegalArgumentNull("provider");

        final char[] chars = string.toCharArray();
        StringBuffer buffer = new StringBuffer();
        boolean properties = false;
        int state = NORMAL;
        int start = 0;
        for (int i = 0; i < chars.length; ++i) {
            char c = chars[i];

            // Dollar sign outside brackets
            if (c == '$' && state != IN_BRACKET)
                state = SEEN_DOLLAR;

            // Open bracket immediatley after dollar
            else if (c == '{' && state == SEEN_DOLLAR) {
                buffer.append(string.substring(start, i - 1));
                state = IN_BRACKET;
                start = i - 1;
            }

            // No open bracket after dollar
            else if (state == SEEN_DOLLAR)
                state = NORMAL;

            // Closed bracket after open bracket
            else if (c == '}' && state == IN_BRACKET) {
                // No content
                if (start + 2 == i) {
                    buffer.append("${}"); // REVIEW: Correct?
                } else
                // Collect the system property
                {
                    String value = null;

                    String key = string.substring(start + 2, i);

                    // check for alias
                    if (FILE_SEPARATOR_ALIAS.equals(key)) {
                        value = FILE_SEPARATOR;
                    } else if (PATH_SEPARATOR_ALIAS.equals(key)) {
                        value = PATH_SEPARATOR;
                    } else {
                        value = provider.getProperty(key);

                        if (value == null) {
                            // Check for a default value ${key:default}
                            int colon = key.indexOf(':');
                            if (colon > 0) {
                                String realKey = key.substring(0, colon);
                                value = provider.getProperty(key);

                                if (value == null) {
                                    // Check for a composite key, "key1,key2"
                                    value = resolveCompositeKey(realKey, provider);

                                    // Not a composite key either, use the specified default
                                    if (value == null)
                                        value = key.substring(colon + 1);
                                }
                            } else {
                                // No default, check for a composite key, "key1,key2"
                                value = resolveCompositeKey(key, provider);
                            }
                        }
                    }

                    if (value != null) {
                        properties = true;
                        buffer.append(value);
                    } else {
                        buffer.append("${");
                        buffer.append(key);
                        buffer.append('}');
                    }

                }
                start = i + 1;
                state = NORMAL;
            }
        }

        // No properties
        if (properties == false)
            return string;

        // Collect the trailing characters
        if (start != chars.length)
            buffer.append(string.substring(start, chars.length));

        // Done
        return buffer.toString();
    }

    /**
     * Try to resolve a "key" from the provided properties by checking if it is actually a "key1,key2", in which case try first
     * "key1", then "key2". If all fails, return null.
     * 
     * It also accepts "key1," and ",key2".
     * 
     * @param key the key to resolve
     * @param props the properties to use
     * @return the resolved key or null
     */
    private static String resolveCompositeKey(String key, PropertyProvider provider) {
        String value = null;

        // Look for the comma
        int comma = key.indexOf(',');
        if (comma > -1) {
            // If we have a first part, try resolve it
            if (comma > 0) {
                // Check the first part
                String key1 = key.substring(0, comma);
                value = provider.getProperty(key1);
            }
            // Check the second part, if there is one and first lookup failed
            if (value == null && comma < key.length() - 1) {
                String key2 = key.substring(comma + 1);
                value = provider.getProperty(key2);
            }
        }
        // Return whatever we've found or null
        return value;
    }
}