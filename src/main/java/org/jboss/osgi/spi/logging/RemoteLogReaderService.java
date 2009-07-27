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
package org.jboss.osgi.spi.logging;

//$Id$

import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogReaderService;

/**
 * An extension of the {@link LogReaderService} that can be setup 
 * to receive remote {@link LogEntry} objects.
 * 
 * @author thomas.diesler@jboss.com
 * @since 12-Apr-2009
 */
public interface RemoteLogReaderService extends LogReaderService
{
   /** Property to set to 'true' on the sending side: 'org.jboss.osgi.service.remote.log.sender' */
   String REMOTE_LOG_SENDER = "org.jboss.osgi.service.remote.log.sender";
   
   /** Property to set to 'true' on the receiving side: 'org.jboss.osgi.service.remote.log.reader' */
   String REMOTE_LOG_READER = "org.jboss.osgi.service.remote.log.reader";
   
   /** Property to set the receiving host: 'org.jboss.osgi.service.remote.log.host' */
   String REMOTE_LOG_HOST = "org.jboss.osgi.service.remote.log.host";
   
   /** Property to set the receiving port: 'org.jboss.osgi.service.remote.log.port' */
   String REMOTE_LOG_PORT = "org.jboss.osgi.service.remote.log.port";
}