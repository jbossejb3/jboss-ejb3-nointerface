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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import javassist.util.proxy.MethodHandler;

/**
 * {@link JavassistInvocationHandlerAdapter} is an implementation of Javassist {@link MethodHandler}
 * and is responsible for forwarding the method invocations to a instance of {@link java.lang.reflect.InvocationHandler}
 *
 * @see MethodHandler
 * @see InvocationHandler
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class JavassistInvocationHandlerAdapter implements MethodHandler
{

   /**
    * The invocation handler to which the method invocations will be forwarded to
    */
   private InvocationHandler invocationHandler;

   /**
    * Creates a {@link JavassistInvocationHandlerAdapter} for an {@link InvocationHandler}
    * @param invocationHandler The invocation handler
    * @throws IllegalArgumentException If the passed <code>invocationHandler</code> is null
    */
   public JavassistInvocationHandlerAdapter(InvocationHandler invocationHandler)
   {
      if (invocationHandler == null)
      {
         throw new IllegalArgumentException(this.getClass().getName() + " cannot be created out of a null "
               + InvocationHandler.class.getName());
      }
      this.invocationHandler = invocationHandler;
   }

   /**
    * Lets the {@link InvocationHandler} instance, which was passed to {@link #JavassistInvocationHandlerAdapter(InvocationHandler)} 
    * handle the method invocation.
    * 
    * @see javassist.util.proxy.MethodHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.reflect.Method, java.lang.Object[])
    */
   @Override
   public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable
   {
      // let the invocation handler take care of the call
      return this.invocationHandler.invoke(self, thisMethod, args);
   }

}
