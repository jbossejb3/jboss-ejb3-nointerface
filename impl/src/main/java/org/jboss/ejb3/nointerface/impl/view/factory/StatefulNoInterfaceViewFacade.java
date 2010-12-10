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
package org.jboss.ejb3.nointerface.impl.view.factory;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;

import org.jboss.dependency.spi.ControllerState;
import org.jboss.ejb3.endpoint.Endpoint;
import org.jboss.ejb3.nointerface.impl.invocationhandler.NoInterfaceViewInvocationHandler;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.jboss.logging.Logger;
import org.jboss.ejb3.proxy.javassist.*;

/**
 * StatefulNoInterfaceViewFacade
 *
 * Responsible for (not necessarily in the following order)
 * - Creating a session from the stateful container
 * - Creating the no-interface view for a stateful session bean
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class StatefulNoInterfaceViewFacade 
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(StatefulNoInterfaceViewFacade.class);

   /**
    * The bean class
    */
   protected Class<?> beanClass;

   /**
    * The KernelControllerContext corresponding to the container of a bean for which
    * the no-interface view is to be created by this factory. This context
    * may <i>not</i> be in INSTALLED state. This factory is responsible
    * for pushing it to INSTALLED state whenever necessary. 
    * 
    */
   protected KernelControllerContext endpointContext;

   /**
    * Constructor
    * @param beanClass
    * @param container
    * @param statefulSessionFactory
    */
   public StatefulNoInterfaceViewFacade(Class<?> beanClass, KernelControllerContext containerContext)
   {
      this.beanClass = beanClass;
      this.endpointContext = containerContext;
   }

   /**
    * First creates a session and then creates a no-interface view for the bean.
    *
    * @return
    * @throws Exception
    */
   public Object createNoInterfaceView() throws Exception
   {
      try
      {
         // first push the endpointContext to INSTALLED
         if (logger.isTraceEnabled())
         {
            logger.trace("Changing the context " + this.endpointContext.getName() + " to state "
                  + ControllerState.INSTALLED.getStateString() + " from current state "
                  + this.endpointContext.getState().getStateString());
         }
         this.endpointContext.getController().change(this.endpointContext, ControllerState.INSTALLED);
      }
      catch (Throwable t)
      {
         throw new RuntimeException("Could not push the context " + this.endpointContext.getName()
               + " from its current state " + this.endpointContext.getState().getStateString() + " to INSTALLED", t);
      }

      // now get hold of the StatefulSessionFactory from the context
      Object target = this.endpointContext.getTarget();
      assert target instanceof Endpoint : "Unexpected object type found " + target + " - expected a " + Endpoint.class;

      Endpoint endpoint = (Endpoint) target;
      if (!endpoint.isSessionAware())
      {
         throw new IllegalStateException("Endpoint " + endpoint
               + " is not session aware. Cannot be used for Stateful no-interface view(s)");
      }

      // create the session
      Serializable session = endpoint.getSessionFactory().createSession(null, null);
      logger.debug("Created session " + session + " for " + this.beanClass);

      // create an invocation handler
      InvocationHandler invocationHandler = new NoInterfaceViewInvocationHandler(this.endpointContext, session, this.beanClass);
      

      // Now create the proxy
      Object noInterfaceView = new JavassistProxyFactory().createProxy(new Class<?>[] {beanClass}, invocationHandler);
      return noInterfaceView;
   }

}
