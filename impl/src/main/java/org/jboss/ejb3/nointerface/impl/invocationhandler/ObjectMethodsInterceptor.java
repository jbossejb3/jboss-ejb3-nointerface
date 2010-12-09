/*
 * JBoss, Home of Professional Open Source.
 * Copyright (c) 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.ejb3.nointerface.impl.invocationhandler;

import org.jboss.ejb3.sis.Interceptor;

import javax.interceptor.InvocationContext;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
class ObjectMethodsInterceptor implements Interceptor
{
   /**
    * Equals and hashCode methods are handled within this invocation handler
    */
   private static final Method METHOD_EQUALS;

   private static final Method METHOD_HASH_CODE;

   private static final Method METHOD_TO_STRING;

   static
   {
      try
      {
         METHOD_EQUALS = Object.class.getDeclaredMethod("equals", Object.class);
         METHOD_HASH_CODE = Object.class.getDeclaredMethod("hashCode");
         METHOD_TO_STRING = Object.class.getDeclaredMethod("toString");
      }
      catch (SecurityException e)
      {
         throw new RuntimeException(e);
      }
      catch (NoSuchMethodException e)
      {
         throw new RuntimeException(e);
      }
   }

   private Object original;

   ObjectMethodsInterceptor(Object original)
   {
      this.original = original;
   }

   @Override
   public Object invoke(InvocationContext context) throws Exception
   {
      Method method = context.getMethod();
      if(method.equals(METHOD_EQUALS))
         return original.equals(context.getParameters()[0]);
      else if(method.equals(METHOD_HASH_CODE))
         return original.hashCode();
      else if(method.equals(METHOD_TO_STRING))
         return original.toString();
      return context.proceed();
   }
}
