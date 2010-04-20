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

import java.util.List;

/**
 * A simple clipboard to pass messages
 * 
 * @author thomas.diesler@jboss.com
 * @since 20-Apr-2010
 */
public interface ClipboardMBean
{
   /** The default object name: jboss.osgi:service=jmx,type=Clipboard */
   String OBJECTNAME = "jboss.osgi:service=jmx,type=Clipboard";

   /**
    * Add a message to the clipboard
    */
   void addMessage(String message);

   /**
    * Get the unmodifieable list of clipboard messages
    */
   List<String> getMessages();

   /**
    * Get the current message count.
    */
   int getMessageCount();

   /**
    * Get the message at the given index.
    */
   String getMessage(int index);

   /**
    * Get the last message added to the list.
    */
   String getLastMessage();

   /**
    * Remove the message at the given index.
    */
   String removeMessage(int index);

   /**
    * Clear the messages on the clipboard.
    */
   void clearMessages();
}