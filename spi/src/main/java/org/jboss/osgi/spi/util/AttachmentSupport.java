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


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jboss.osgi.spi.Attachments;

/**
 * Basic attachment support.
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 20-Apr-2007
 */
public abstract class AttachmentSupport implements Attachments
{
   private Map<Key, Object> attachments = new HashMap<Key, Object>();

   /** Construct with no attachments */
   public AttachmentSupport()
   {
   }

   /** Construct with given attachments */
   public AttachmentSupport(AttachmentSupport att)
   {
      attachments = att.attachments;
   }

   public Collection<Key> getAttachmentKeys()
   {
      return attachments.keySet();
   }

   @SuppressWarnings("unchecked")
   public <T> T getAttachment(Class<T> clazz)
   {
      return (T)attachments.get(new Key(null, clazz));
   }

   @SuppressWarnings("unchecked")
   public <T> T getAttachment(String name, Class<T> clazz)
   {
      return (T)attachments.get(new Key(name, clazz));
   }

   public Object getAttachment(String name)
   {
      return attachments.get(new Key(name, null));
   }

   @SuppressWarnings("unchecked")
   public <T> T addAttachment(Class<T> clazz, T obj)
   {
      return (T)attachments.put(new Key(null, clazz), obj);
   }

   @SuppressWarnings("unchecked")
   public <T> T addAttachment(String name, T obj, Class<T> clazz)
   {
      return (T)attachments.put(new Key(name, clazz), obj);
   }

   public Object addAttachment(String name, Object obj)
   {
      return attachments.put(new Key(name, null), obj);
   }

   @SuppressWarnings("unchecked")
   public <T> T removeAttachment(Class<T> clazz)
   {
      return (T)attachments.remove(new Key(null, clazz));
   }

   @SuppressWarnings("unchecked")
   public <T> T removeAttachment(Class<T> clazz, String name)
   {
      return (T)attachments.remove(new Key(name, clazz));
   }

   public Object removeAttachment(String name)
   {
      return attachments.remove(new Key(name, null));
   }

   public String toString()
   {
      return attachments.toString();
   }
}
