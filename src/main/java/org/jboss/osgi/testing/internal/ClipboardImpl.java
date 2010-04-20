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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;

import org.jboss.logging.Logger;
import org.jboss.osgi.testing.ClipboardMBean;

/**
 * An implementation of a simple clipboard.
 * 
 * @author thomas.diesler@jboss.com
 * @since 20-Apr-2010
 */
public class ClipboardImpl extends StandardMBean implements ClipboardMBean
{
   // Provide logging
   private static final Logger log = Logger.getLogger(ClipboardImpl.class);
   
   private List<String> messages = new ArrayList<String>();
   
   public ClipboardImpl() throws NotCompliantMBeanException
   {
      super(ClipboardMBean.class);
   }

   @Override
   public void addMessage(String message)
   {
      log.info("addMessage: " + message);
      messages.add(message);
   }

   @Override
   public List<String> getMessages()
   {
      return Collections.unmodifiableList(messages);
   }

   @Override
   public int getMessageCount()
   {
      return messages.size();
   }
   
   @Override
   public String getMessage(int index)
   {
      return messages.get(index);
   }
   
   @Override
   public String getLastMessage()
   {
      int count = getMessageCount();
      return count > 0 ? messages.get(count - 1) : null;
   }
   
   @Override
   public String removeMessage(int index)
   {
      String message = messages.get(index);
      log.info("removeMessage: " + message);
      return message;
   }
   
   @Override
   public void clearMessages()
   {
      log.info("clearMessages");
      messages.clear();
   }
}