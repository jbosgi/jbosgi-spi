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
package org.jboss.osgi.spi;

// $Id$

import java.util.Collection;

/**
 * An interface for general Attachments
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 20-Apr-2007
 */
public interface Attachments
{
   /** Get attachment keys */
   Collection<Key> getAttachmentKeys();

   /** Add arbitrary attachment */
   <T> T addAttachment(Class<T> clazz, T value);

   /** Add arbitrary attachment with name */
   <T> T addAttachment(String name, T value, Class<T> clazz);

   /** Add arbitrary attachment with name */
   Object addAttachment(String name, Object value);

   /** Get an arbitrary attachment */
   <T> T getAttachment(Class<T> clazz);

   /** Get an arbitrary attachment */
   <T> T getAttachment(String name, Class<T> clazz);

   /** Get an arbitrary attachment */
   Object getAttachment(String name);

   /** Remove arbitrary attachments */
   <T> T removeAttachment(Class<T> clazz);

   /** Remove arbitrary attachments */
   <T> T removeAttachment(Class<T> clazz, String name);

   /** Remove arbitrary attachments */
   Object removeAttachment(String name);

   /**
    * A key for attachements
    */
   public static class Key
   {
      private Class<?> clazz;
      private String name;

      /**
       * Construct the key with optional class and name
       */
      public Key(String name, Class<?> clazz)
      {
         this.clazz = clazz;
         this.name = name;
      }

      public static Key valueOf(String key)
      {
         int index = key.indexOf(",");
         if (key.startsWith("[") && key.endsWith("]") && index > 0)
         {
            Class<?> classPart = null;
            String className = key.substring(1, index);
            String namePart = key.substring(index + 1, key.length() - 1);
            if (className.length() > 0 && !className.equals("null"))
            {
               try
               {
                  classPart = Class.forName(className);
               }
               catch (ClassNotFoundException ex)
               {
                  throw new IllegalArgumentException("Cannot find class '" + className + "' in: " + key);
               }
            }
            return new Key(namePart, classPart);
         }
         return null;
      }

      /**
       * Get the class part for this key
       * 
       * @return maybe null
       */
      public Class<?> getClassPart()
      {
         return clazz;
      }

      /**
       * Get the name part for this key
       * 
       * @return maybe null
       */
      public String getNamePart()
      {
         return name;
      }

      /**
       * Two keys are equal if their {@link #toString()} is equal
       */
      public boolean equals(Object obj)
      {
         if (!(obj instanceof Key))
            return false;
         if (obj == this)
            return true;
         return obj.toString().equals(toString());
      }

      /**
       * Two keys have the same hashCode if their {@link #toString()} is equal
       */
      public int hashCode()
      {
         return toString().hashCode();
      }

      /**
       * Returns the String repesentation of this Key.
       * <p/>
       * 
       * <pre>
       * &quot;[&quot; + clazz + &quot;,&quot; + name + &quot;]&quot;
       * </pre>
       */
      public String toString()
      {
         return "[" + clazz + "," + name + "]";
      }
   }
}