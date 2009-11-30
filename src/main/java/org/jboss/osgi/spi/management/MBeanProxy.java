/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.osgi.spi.management;

// $Id$

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;

import javax.management.Attribute;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeErrorException;
import javax.management.RuntimeMBeanException;
import javax.management.RuntimeOperationsException;

/**
 * A simple MBeanProxy
 *
 * @author  Thomas.Diesler@jboss.com
 * @since 24-Feb-2009
 */
public class MBeanProxy
{
   @SuppressWarnings({ "unchecked" })
   public static <T> T get(Class<T> interf, ObjectName name, MBeanServerConnection server) throws MBeanProxyException
   {
      return (T)get(new Class[] { interf }, name, server);
   }

   @SuppressWarnings({ "rawtypes" })
   public static Object get(Class[] interfaces, ObjectName name, MBeanServerConnection server) throws MBeanProxyException
   {
      if (interfaces == null || interfaces.length == 0)
         throw new IllegalArgumentException("Null interfaces");

      ClassLoader classLoader = interfaces[0].getClassLoader();
      return Proxy.newProxyInstance(classLoader, interfaces, new JMXInvocationHandler(server, name));
   }

   /**
    * Invocation handler for MBean proxies.
    *
    * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>
    * @author  Thomas.Diesler@jboss.com
    * @since 24-Feb-2009
    */
   static class JMXInvocationHandler implements InvocationHandler, Serializable
   {
      private static final long serialVersionUID = 3714728148040623702L;

      // Attributes -------------------------------------------------

      /*
       * Reference to the MBean server this proxy connects to.
       */
      protected MBeanServerConnection server = null;

      /*
       * The object name of the MBean this proxy represents.
       */
      protected ObjectName objectName = null;

      /*
       * MBean attribute meta data.
       */
      private HashMap<String, MBeanAttributeInfo> attributeMap = new HashMap<String, MBeanAttributeInfo>();

      /*
       * Indicates whether Object.toString() should be delegated to the resource or handled by the proxy.
       */
      private boolean delegateToStringToResource = false;

      /*
       * Indicates whether Object.equals() should be delegated to the resource or handled by the proxy.
       */
      private boolean delegateEqualsToResource = false;

      /*
       * Indicates whether Object.hashCode() should be delegated to the resource or handled by the proxy.
       */
      private boolean delegateHashCodeToResource = false;

      // Constructors -----------------------------------------------

      /*
       * Constructs a new JMX MBean Proxy invocation handler.
       * @param server reference to the MBean server this proxy connects to
       * @param name object name of the MBean this proxy represents
       * @throws MBeanProxyCreationException wraps underlying JMX exceptions in case the proxy creation fails
       */
      public JMXInvocationHandler(MBeanServerConnection server, ObjectName name) throws MBeanProxyException
      {
         try
         {
            if (server == null)
               throw new MBeanProxyException("null agent reference");

            if (name == null)
               throw new MBeanProxyException("null object name");

            this.server = server;
            this.objectName = name;

            MBeanInfo info = server.getMBeanInfo(objectName);
            MBeanAttributeInfo[] attributes = info.getAttributes();
            MBeanOperationInfo[] operations = info.getOperations();

            // collect the MBean attribute metadata for standard mbean proxies
            for (int i = 0; i < attributes.length; ++i)
               attributeMap.put(attributes[i].getName(), attributes[i]);

            // Check whether the target resource exposes the common object methods.
            // Dynamic Proxy will delegate these methods automatically to the
            // invoke() implementation.
            for (int i = 0; i < operations.length; ++i)
            {
               if (operations[i].getName().equals("toString") && operations[i].getReturnType().equals("java.lang.String")
                     && operations[i].getSignature().length == 0)
               {
                  delegateToStringToResource = true;
               }

               else if (operations[i].getName().equals("equals") && operations[i].getReturnType().equals(Boolean.TYPE.getName())
                     && operations[i].getSignature().length == 1 && operations[i].getSignature()[0].getType().equals("java.lang.Object"))
               {
                  delegateEqualsToResource = true;
               }

               else if (operations[i].getName().equals("hashCode") && operations[i].getReturnType().equals(Integer.TYPE.getName())
                     && operations[i].getSignature().length == 0)
               {
                  delegateHashCodeToResource = true;
               }
            }
         }
         catch (InstanceNotFoundException e)
         {
            throw new MBeanProxyException("Object name " + name + " not found: " + e.toString());
         }
         catch (Exception ex)
         {
            throw new MBeanProxyException(ex.toString());
         }
      }

      // InvocationHandler implementation ---------------------------

      @SuppressWarnings("rawtypes")
      public Object invoke(Object proxy, Method method, Object[] args) throws Exception
      {
         Class<?> declaringClass = method.getDeclaringClass();

         // Handle methods from Object class. If the target resource exposes
         // operation metadata with same signature then the invocations will be
         // delegated to the target. Otherwise this instance of invocation handler
         // will execute them.
         if (declaringClass == Object.class)
            return handleObjectMethods(method, args);

         try
         {
            String methodName = method.getName();

            // Assume a get/setAttribute convention on the typed proxy interface.
            // If the MBean metadata exposes a matching attribute then use the
            // MBeanServer attribute accessors to read/modify the value. If not,
            // fallback to MBeanServer.invoke() assuming this is an operation
            // invocation despite the accessor naming convention.

            // getter
            if (methodName.startsWith("get") && args == null)
            {
               String attrName = methodName.substring(3, methodName.length());

               // check that the metadata exists
               MBeanAttributeInfo info = (MBeanAttributeInfo)attributeMap.get(attrName);
               if (info != null)
               {
                  String retType = method.getReturnType().getName();

                  // check for correct return type on the getter
                  if (retType.equals(info.getType()))
                  {
                     return server.getAttribute(objectName, attrName);
                  }
               }
            }

            // boolean getter
            else if (methodName.startsWith("is") && args == null)
            {
               String attrName = methodName.substring(2, methodName.length());

               // check that the metadata exists
               MBeanAttributeInfo info = (MBeanAttributeInfo)attributeMap.get(attrName);
               if (info != null && info.isIs())
               {
                  Class<?> retType = method.getReturnType();

                  // check for correct return type on the getter
                  if (retType.equals(Boolean.class) || retType.equals(Boolean.TYPE))
                  {
                     return server.getAttribute(objectName, attrName);
                  }
               }
            }

            // setter
            else if (methodName.startsWith("set") && args != null && args.length == 1)
            {
               String attrName = methodName.substring(3, methodName.length());

               // check that the metadata exists
               MBeanAttributeInfo info = (MBeanAttributeInfo)attributeMap.get(attrName);
               if (info != null && method.getReturnType().equals(Void.TYPE))
               {
                  ClassLoader cl = Thread.currentThread().getContextClassLoader();

                  Class<?> signatureClass = null;
                  String classType = info.getType();

                  if (isPrimitive(classType))
                     signatureClass = getPrimitiveClass(classType);
                  else
                     signatureClass = cl.loadClass(info.getType());

                  if (signatureClass.isAssignableFrom(args[0].getClass()))
                  {
                     server.setAttribute(objectName, new Attribute(attrName, args[0]));
                     return null;
                  }
               }
            }

            String[] signature = null;

            if (args != null)
            {
               signature = new String[args.length];
               Class[] sign = method.getParameterTypes();

               for (int i = 0; i < sign.length; ++i)
                  signature[i] = sign[i].getName();
            }

            return server.invoke(objectName, methodName, args, signature);
         }
         catch (Exception ex)
         {
            throw (Exception)decodeJMXException(ex);
         }
      }

      /*
       * Attempt to decode the given Throwable. 
       * If it is a container JMX exception, then the target is returned. 
       * Otherwise the argument is returned.
       */
      private Throwable decodeJMXException(final Exception ex)
      {
         Throwable result = ex;

         while (true)
         {
            if (result instanceof MBeanException)
               result = ((MBeanException)result).getTargetException();
            else if (result instanceof ReflectionException)
               result = ((ReflectionException)result).getTargetException();
            else if (result instanceof RuntimeOperationsException)
               result = ((RuntimeOperationsException)result).getTargetException();
            else if (result instanceof RuntimeMBeanException)
               result = ((RuntimeMBeanException)result).getTargetException();
            else if (result instanceof RuntimeErrorException)
               result = ((RuntimeErrorException)result).getTargetError();
            else
               // can't decode
               break;
         }

         return result;
      }

      private Object handleObjectMethods(Method method, Object[] args) throws Exception
      {
         if (method.getName().equals("toString"))
         {
            if (delegateToStringToResource)
               return server.invoke(objectName, "toString", null, null);
            else
               return toString();
         }

         else if (method.getName().equals("equals"))
         {
            if (delegateEqualsToResource)
            {
               return server.invoke(objectName, "equals", new Object[] { args[0] }, new String[] { "java.lang.Object" });
            }
            else if (Proxy.isProxyClass(args[0].getClass()))
            {
               Proxy prxy = (Proxy)args[0];
               return new Boolean(this.equals(Proxy.getInvocationHandler(prxy)));
            }
            else
            {
               return new Boolean(this.equals(args[0]));
            }
         }

         else if (method.getName().equals("hashCode"))
         {
            if (delegateHashCodeToResource)
               return server.invoke(objectName, "hashCode", null, null);
            else
               return new Integer(this.hashCode());
         }

         else
            throw new Error("Unexpected method invocation!");
      }

      private boolean isPrimitive(String type)
      {
         if (type.equals(Integer.TYPE.getName()))
            return true;
         if (type.equals(Long.TYPE.getName()))
            return true;
         if (type.equals(Boolean.TYPE.getName()))
            return true;
         if (type.equals(Byte.TYPE.getName()))
            return true;
         if (type.equals(Character.TYPE.getName()))
            return true;
         if (type.equals(Short.TYPE.getName()))
            return true;
         if (type.equals(Float.TYPE.getName()))
            return true;
         if (type.equals(Double.TYPE.getName()))
            return true;
         if (type.equals(Void.TYPE.getName()))
            return true;

         return false;
      }

      private Class<?> getPrimitiveClass(String type)
      {
         if (type.equals(Integer.TYPE.getName()))
            return Integer.TYPE;
         if (type.equals(Long.TYPE.getName()))
            return Long.TYPE;
         if (type.equals(Boolean.TYPE.getName()))
            return Boolean.TYPE;
         if (type.equals(Byte.TYPE.getName()))
            return Byte.TYPE;
         if (type.equals(Character.TYPE.getName()))
            return Character.TYPE;
         if (type.equals(Short.TYPE.getName()))
            return Short.TYPE;
         if (type.equals(Float.TYPE.getName()))
            return Float.TYPE;
         if (type.equals(Double.TYPE.getName()))
            return Double.TYPE;
         if (type.equals(Void.TYPE.getName()))
            return Void.TYPE;

         return null;
      }
   }
}
