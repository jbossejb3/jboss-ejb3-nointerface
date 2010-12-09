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
package org.jboss.ejb3.nointerface.impl.invocationhandler;

import org.jboss.ejb3.endpoint.Endpoint;
import org.jboss.ejb3.sis.Interceptor;
import org.jboss.ejb3.sis.reflect.InterceptorInvocationHandler;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.jboss.logging.Logger;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isNative;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;

/**
 * NoInterfaceViewInvocationHandler
 *
 * An {@link InvocationHandler} which corresponds to the
 * no-interface view of a EJB container. All calls on the no-interface
 * view are routed through this {@link InvocationHandler} to the container.
 *
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class NoInterfaceViewInvocationHandler implements InvocationHandler
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(NoInterfaceViewInvocationHandler.class);

   /**
    * The KernelControllerContext corresponding to the endpoint for which
    * the no-interface view is to be created by this factory. This context
    * may <i>not</i> be in INSTALLED state. This factory is responsible
    * for pushing it to INSTALLED state whenever necessary. 
    * 
    * All calls to this invocation handler will be forwarded to the container represented
    * by this context
    * 
    *
    */
   private KernelControllerContext endpointContext;

   /**
    * The session used to interact with the {@link Endpoint}
    */
   private Serializable session;

   /**
    * The business interface (== bean class, since this is a no-interface view) on
    * which the proxy invocation happens.
    */
   private Class<?> businessInterface;

   private InvocationHandler delegate;
   
   /**
    * Constructor
    */
   public NoInterfaceViewInvocationHandler(KernelControllerContext endpointContext, Serializable session, Class<?> businessInterface)
   {
      assert endpointContext != null : "Endpoint context is null for no-interface view invocation handler";
      this.endpointContext = endpointContext;
      this.session = session;
      this.businessInterface = businessInterface;

      InvocationHandler endpointInvocationHandler = new InvocationHandler()
      {
         @Override
         public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
         {
            return invokeEndpoint(proxy, method, args);
         }
      };
      Interceptor interceptor = new ObjectMethodsInterceptor(this);
      this.delegate = new InterceptorInvocationHandler(endpointInvocationHandler, interceptor);
   }

   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      return delegate.invoke(proxy, method, args);
   }

   /**
    * The entry point when a client calls any methods on the no-interface view of a bean,
    * returned through JNDI.
    *
    *
    * @param proxy
    * @param method The invoked method
    * @param args The arguments to the method
    */
   private Object invokeEndpoint(Object proxy, Method method, Object[] args) throws Throwable
   {
      // check to see if this method is expected to be handled
      // by the nointerface view (for ex: only public methods of bean are allowed
      // on nointerface view)
      if (!isHandled(method))
      {
         throw new javax.ejb.EJBException("Cannot invoke method " + method + " on nointerface view");
      }

      // get the endpoint (which will involve pushing it to INSTALLED state)
      Endpoint endpoint = getInstalledEndpoint();
      assert endpoint != null : "No endpoint associated with context " + this.endpointContext
            + " - cannot invoke the method on bean";

      // finally pass-on the control to the endpoint
      return endpoint.invoke(this.session, this.businessInterface, method, args);
   }

   /**
    * Returns the context corresponding to the container, associated with this invocation handler
    *
    * @return
    */
   public KernelControllerContext getContainerContext()
   {
      return this.endpointContext;
   }

   /**
    * Returns the {@link Endpoint} container corresponding to this 
    * {@link NoInterfaceViewInvocationHandler}. Internally, the {@link Endpoint} 
    * will be first pushed to the INSTALLED state 
    * 
    * @return
    */
   public Endpoint getInstalledEndpoint()
   {
      try
      {
         // EJBTHREE-2166 - Changing state through MC API won't work. So for now,
         // we are going to rely on an already INSTALLED endpoint context
//         this.endpointContext.getController().change(this.endpointContext, ControllerState.INSTALLED);
         
         // get hold of the endpoint from its context
         Endpoint endpoint = (Endpoint) this.endpointContext.getTarget();
         return endpoint;
      }
      catch (Throwable t)
      {
         throw new RuntimeException("Error getting endpoint out of container KernelControllerContext "
               + this.endpointContext, t);
      }

   }

   /**
    * Returns the {@link Endpoint} container corresponding to this 
    * {@link NoInterfaceViewInvocationHandler}. Note that this method does NOT
    * change the state of the KernelControllerContext of this Endpoint. As such,
    * the Endpoint returned by this method is NOT guaranteed to be in INSTALLED state.
    * If the Endpoint with INSTALLED state is required, then use the {@link #getInstalledEndpoint()}
    * method. 
    *  
    * @return
    * @see #getInstalledEndpoint()
    */
   private Endpoint getEndpoint()
   {
      Object endpoint = this.endpointContext.getTarget();
      assert endpoint instanceof Endpoint : "Unexpected type " + endpoint.getClass().getName() + " found in context "
            + this.endpointContext + " Expected " + Endpoint.class.getName();
      return (Endpoint) endpoint;
   }

   /**
    * @see Object#equals(Object)
    */
   @Override
   public boolean equals(Object other)
   {
      // simple object comparison
      if (this == other)
      {
         return true;
      }

      // equals() method contract specifies that if the other object is null
      // then equals() should return false
      if (other == null)
      {
         return false;
      }

      // If the other object is not an instance of MCAwareNoInterfaceViewInvocationHandler
      // then they are not equal
      if (!(other instanceof NoInterfaceViewInvocationHandler))
      {
         return other.equals(this);
      }

      NoInterfaceViewInvocationHandler otherNoInterfaceViewInvocationHandler = (NoInterfaceViewInvocationHandler) other;

      // First check whether the Endpoints of both these InvocationHandlers are equal. If 
      // not, then no need for any further comparison, just return false
      if (!(this.getInstalledEndpoint().equals(otherNoInterfaceViewInvocationHandler.getInstalledEndpoint())))
      {
         return false;
      }

      // If the endpoints are equal, then let's next check whether the sessions for
      // these invocation handlers are equal. If not, return false.
      if (!(this.session.equals(otherNoInterfaceViewInvocationHandler.session)))
      {
         return false;
      }
      // All possible, inequality conditions have been tested, so return true.
      return true;
   }

   /**
    * @see Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      int hashCode = this.endpointContext.hashCode();
      if (this.session != null)
      {
         hashCode += this.session.hashCode();
      }
      return hashCode;
   }

   /**
    * @see Object#toString()
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder("No-Interface view for endpoint [ " + endpointContext.getName() + " ]");
      if (this.session != null)
      {
         sb.append(" and session " + this.session);
      }
      return sb.toString();
   }

   /**
    * 
    * @param method
    * @return
    */
   public boolean isHandled(Method method)
   {
      int m = method.getModifiers();
      // We handle only public, non-static, non-final methods
      if (!isPublic(m))
      {
         if (logger.isTraceEnabled())
         {
            logger.trace("Method " + method + " is *not* public");
         }
         // it's not a public method
         return false;
      }
      if (isFinal(m))
      {
         if (logger.isTraceEnabled())
         {
            logger.trace("Method " + method + " is final");
         }
         // it's a final method
         return false;
      }
      if (isStatic(m))
      {
         if (logger.isTraceEnabled())
         {
            logger.trace("Method " + method + " is static");
         }
         // it's a static method
         return false;
      }
      if (isNative(m))
      {
         if (logger.isTraceEnabled())
         {
            logger.trace("Method " + method + " is native");
         }
         // it's a native method
         return false;
      }
      // we handle rest of the methods
      return true;
   }
}
