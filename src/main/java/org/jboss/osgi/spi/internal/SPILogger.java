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

package org.jboss.osgi.spi.internal;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Cause;
import org.jboss.logging.LogMessage;
import org.jboss.logging.Logger;
import org.jboss.logging.Message;
import org.jboss.logging.MessageLogger;
import org.osgi.framework.Bundle;

import static org.jboss.logging.Logger.Level.ERROR;
import static org.jboss.logging.Logger.Level.INFO;
import static org.jboss.logging.Logger.Level.WARN;

/**
 * Logging Id ranges: 10200-10299
 *
 * https://docs.jboss.org/author/display/JBOSGI/JBossOSGi+Logging
 *
 * @author Thomas.Diesler@jboss.com
 */
@MessageLogger(projectCode = "JBOSGI")
public interface SPILogger extends BasicLogger {

    SPILogger LOGGER = Logger.getMessageLogger(SPILogger.class, "org.jboss.osgi.spi");

    @LogMessage(level = INFO)
    @Message(id = 10200, value = "Bundle installed [%d]: %s")
    void infoBundleInstalled(long bundleId, Bundle bundle);

    @LogMessage(level = INFO)
    @Message(id = 10201, value = "Bundle started [%d]: %s")
    void infoBundleStarted(long bundleId, Bundle bundle);

    @LogMessage(level = INFO)
    @Message(id = 10202, value = "JBossOSGi Runtime booted in %fsec")
    void infoRuntimeBooted(float seconds);

    @LogMessage(level = INFO)
    @Message(id = 10203, value = "Initiating shutdown ...")
    void infoInitiatingShutdown();

    @LogMessage(level = INFO)
    @Message(id = 10204, value = "Shutdown complete")
    void infoShutdownComplete();

    @LogMessage(level = WARN)
    @Message(id = 10205, value = "Service not assignable: %s")
    void warnServiceNotAssignable(String classname);

    @LogMessage(level = ERROR)
    @Message(id = 10206, value = "Cannot load property instance [%s=%s]")
    void errorCannotLoadPropertyInstance(@Cause Throwable cause, String key, String value);

    @LogMessage(level = ERROR)
    @Message(id = 10207, value = "Cannot stop framework")
    void errorCannotStopFramework(@Cause Throwable cause);
}
