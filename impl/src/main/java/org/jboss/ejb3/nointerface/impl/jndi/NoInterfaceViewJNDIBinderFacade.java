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
package org.jboss.ejb3.nointerface.impl.jndi;

import javax.naming.Context;

import org.jboss.beans.metadata.api.annotations.Start;
import org.jboss.beans.metadata.api.annotations.Stop;
import org.jboss.ejb3.nointerface.spi.jndi.NoInterfaceViewJNDIBinder;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossSessionBean31MetaData;

/**
 * NoInterfaceViewJNDIBinderFacade
 *
 * A {@link NoInterfaceViewJNDIBinderFacade} corresponds to a EJB which is eligible
 * for a no-interface view
 * 
 * This MC bean has dependencies (like the container) injected as necessary.
 * During its START phase this NoInterfaceViewJNDIBinder creates a no-interface view
 * and binds it to the jndi. 
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class NoInterfaceViewJNDIBinderFacade
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(NoInterfaceViewJNDIBinderFacade.class);

   /**
    * The endpoint for which this {@link NoInterfaceViewJNDIBinderFacade} holds
    * an no-interface view
    */
   // Bean name will be added to this Inject by the deployer.
   protected KernelControllerContext endpointContext;

   /**
    * The bean class for which the no-interface view corresponds
    */
   private Class<?> beanClass;

   /**
    * The bean metadata
    */
   private JBossSessionBean31MetaData sessionBeanMetadata;

   /**
    * JNDI naming context
    */
   private Context jndiCtx;
   
   /**
    * JNDI binder
    */
   private NoInterfaceViewJNDIBinder binder;

   /**
    * Constructor
    *
    * @param beanClass
    * @param sessionBeanMetadata
    */
   public NoInterfaceViewJNDIBinderFacade(Context ctx, Class<?> beanClass,
         JBossSessionBean31MetaData sessionBeanMetadata)
   {
      this.jndiCtx = ctx;
      this.beanClass = beanClass;
      this.sessionBeanMetadata = sessionBeanMetadata;
      

   }

   /**
    * Will be called when the dependencies of this {@link NoInterfaceViewJNDIBinderFacade} are
    * resolved and this MC bean reaches the START state.
    *
    * At this point, the {@link #endpointContext} associated with this {@link NoInterfaceViewJNDIBinderFacade}
    * is injected and is at a minimal of DESCRIBED state. We now create a no-interface view
    * for the corresponding bean.
    * Note: No validations (like whether the bean is eligible for no-interface view) is done at this
    * stage. It's assumed that the presence of a {@link NoInterfaceViewJNDIBinderFacade} indicates that the
    * corresponding bean is eligible for no-interface view.
    *
    * @throws Exception
    */
   @Start
   public void onStart() throws Exception
   {
      if (logger.isTraceEnabled())
      {
         logger.trace("Creating no-interface view for endpoint " + this.endpointContext);
      }

      if (this.sessionBeanMetadata.isStateful())
      {
         this.binder = new SessionAwareNoInterfaceViewJNDIBinder(this.endpointContext);
      }
      else 
      {
         this.binder = new SessionlessNoInterfaceViewJNDIBinder(this.endpointContext);
      }

      this.binder.bindNoInterfaceView(this.jndiCtx, this.beanClass, this.sessionBeanMetadata);
   }

   /**
    * Does any relevant cleanup
    *  
    * @throws Exception
    */
   @Stop
   public void onStop() throws Exception
   {
      if (logger.isTraceEnabled())
      {
         logger.trace("Unbinding no-interface view from JNDI, for endpoint " + this.endpointContext);
      }
      if (this.binder != null)
      {
         this.binder.unbindNoInterfaceView(this.jndiCtx, this.beanClass, this.sessionBeanMetadata);
      }
   }

   /**
    * 
    * @param endpointContext The KernelControllerContext corresponding to the endpoint
    * @throws Exception
    */
   public void setEndpointContext(KernelControllerContext endpointContext) throws Exception
   {
      this.endpointContext = endpointContext;

   }

}
