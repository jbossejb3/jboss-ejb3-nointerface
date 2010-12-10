/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.nointerface.impl.async;

import java.io.Serializable;
import java.lang.reflect.Method;

import javax.interceptor.InvocationContext;

import org.jboss.ejb3.async.spi.AsyncEndpoint;
import org.jboss.ejb3.async.spi.AsyncUtil;
import org.jboss.ejb3.sis.Interceptor;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.spec.AsyncMethodsMetaData;

/**
 * No-interface implementation of a client interceptor to dispatch
 * asynchronous invocations
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 */
public class AsyncClientInterceptor implements Interceptor
{
   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(AsyncClientInterceptor.class);

   /**
    * View of the container
    */
   private final AsyncEndpoint asyncEndpoint;

   /**
    * Async Methods for this Bean
    */
   private final AsyncMethodsMetaData asyncMethods;

   /**
    * Session ID for SFSB)
    */
   private final Serializable sessionId;

   /**
    * Constructor
    * @param asyncEndpoint View of the container
    * @param asyncMethods @Asynchronous Methods for this EJB
    */
   public AsyncClientInterceptor(final AsyncEndpoint asyncEndpoint, final AsyncMethodsMetaData asyncMethods,
         final Serializable sessionId)
   {
      // Precondition checks
      if (asyncEndpoint == null)
      {
         throw new IllegalArgumentException("Endpoint must be specified");
      }
      // Precondition checks
      if (asyncMethods == null)
      {
         throw new IllegalArgumentException("asyncMethods must be specified");
      }

      // Set
      this.asyncEndpoint = asyncEndpoint;
      this.asyncMethods = asyncMethods;
      this.sessionId = sessionId; // Allowed to be null; SLSB and Singleton
   }

   /**
    * {@inheritDoc}
    * @see org.jboss.ejb3.sis.Interceptor#invoke(javax.interceptor.InvocationContext)
    */
   public Object invoke(final InvocationContext context) throws Exception
   {

      // Determine if async
      if (this.isAsyncInvocation(context))
      {
         // Invoke async-ey via the container
         //TODO Hack alert.  We're skipping all other interceptors in the chain at this point,
         // but there's no facility to copy the invocation context without digging
         // into internals.
         try
         {
            return asyncEndpoint.invokeAsync(sessionId, null, context.getMethod(), context.getParameters());
         }
         catch (final RuntimeException re)
         {
            // Allow this to pass through unchanged
            throw re;
         }
         catch (final Throwable e)
         {
            throw new RuntimeException("Encountered an error dispatching asynchronous invocation: " + context, e);
         }

      }

      // Else forward along
      return context.proceed();
   }

   /**
    * Returns if the invoked method is asynchronous
    * @param context
    * @return
    */
   private boolean isAsyncInvocation(final InvocationContext context)
   {
      // Precondition checks
      assert context != null : "context must be supplied";

      // Extract the method
      final Method invokedMethod = context.getMethod();

      // Async?
      return AsyncUtil.methodIsAsynchronous(invokedMethod, asyncMethods);
   }
}
