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
package org.jboss.ejb3.nointerface.impl.view;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;

/**
 * A {@link NoInterfaceViewMethodFilter} is responsible for deciding whether
 * a method invoked on a nointerface view proxy is to be handled by the 
 * underlying invocation handler. 
 * <p>
 *  The {@link NoInterfaceViewMethodFilter#isHandled(Method)} checks for the method attributes to
 *  decide whether the method should be skipped by the proxy's {@link MethodHandler} or whether it should be handled
 *  by the proxy's {@link MethodHandler}
 * </p>
 *
 * @see MethodHandler
 * @see MethodFilter
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class NoInterfaceViewMethodFilter implements MethodFilter
{

   /**
    * Returns true if the {@link Method} <code>m</code> should be handled 
    * by the nointerface view proxy's invocation handler.
    * 
    * <p>
    *   Returns false if 
    *   <ul>
    *       <li><code>m</code> is *not* public</li>
    *       <li><code>m</code> is static</li>
    *       <li><code>m</code> is final</li>
    *       <li><code>m</code> is native</li> 
    *   </ul>
    * </p>
    * @see javassist.util.proxy.MethodFilter#isHandled(java.lang.reflect.Method)
    */
   @Override
   public boolean isHandled(Method m)
   {
      // We handle only public, non-static, non-final methods
      if (!isPublic(m))
      {
         // it's not a public method
         return false;
      }
      if (isFinal(m))
      {
         // it's a final method
         return false;
      }
      if (isStatic(m))
      {
         // it's a static method
         return false;
      }
      if (isNative(m))
      {
         // it's a native method
         return false;
      }
      // we handle rest of the methods
      return true;
   }

   /**
    * Returns true if the {@link Method} <code>m</code> is a public method.
    * Else returns false
    * 
    * @param m The method
    * @return
    */
   private boolean isPublic(Method m)
   {
      int modifiers = m.getModifiers();
      if ((Modifier.PUBLIC & modifiers) == Modifier.PUBLIC)
      {
         return true;
      }
      return false;
   }

   /**
    * Returns true if the {@link Method} <code>m</code> is a final method.
    * Else returns false
    * 
    * @param m The method
    * @return
    */
   private boolean isFinal(Method m)
   {
      int modifiers = m.getModifiers();
      if ((Modifier.FINAL & modifiers) == Modifier.FINAL)
      {
         return true;
      }
      return false;
   }

   /**
    * Returns true if the {@link Method} <code>m</code> is a static method.
    * Else returns false
    * 
    * @param m The method
    * @return
    */
   private boolean isStatic(Method m)
   {
      int modifiers = m.getModifiers();
      if ((Modifier.STATIC & modifiers) == Modifier.STATIC)
      {
         return true;
      }
      return false;
   }

   /**
    * Returns true if the {@link Method} <code>m</code> is a native method.
    * Else returns false
    * 
    * @param m The method
    * @return
    */
   private boolean isNative(Method m)
   {
      int modifiers = m.getModifiers();
      if ((Modifier.NATIVE & modifiers) == Modifier.NATIVE)
      {
         return true;
      }
      return false;
   }
}
