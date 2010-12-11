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

import java.lang.reflect.InvocationHandler;

import javax.naming.Context;
import javax.naming.NamingException;

import org.jboss.ejb3.nointerface.impl.invocationhandler.NoInterfaceViewInvocationHandler;
import org.jboss.ejb3.proxy.javassist.JavassistProxyFactory;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossSessionBean31MetaData;
import org.jboss.metadata.ejb.spec.AsyncMethodsMetaData;
import org.jboss.util.naming.NonSerializableFactory;

/**
 * Responsible for binding a nointerface view proxy into jndi, for
 * EJBs which are *not* session aware (ex: Stateless beans and Singleton beans)
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class SessionlessBeanNoInterfaceViewBinder extends AbstractNoInterfaceViewBinder
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(SessionlessBeanNoInterfaceViewBinder.class);

   /**
    * Constructor
    * 
    * @param ctx
    * @param beanClass
    * @param sessionBeanMetadata
    */
   public SessionlessBeanNoInterfaceViewBinder(Context jndiCtx, String jndiName, Class<?> beanClass, JBossSessionBean31MetaData beanMetaData)
   {
      super(jndiCtx, jndiName, beanClass, beanMetaData);
   }

   /**
    * Creates the no-interface view for the bean and binds it to the JNDI
    * under the no-interface view jndi name obtained from <code>sessionBeanMetadata</code>.
    *
    * @see JavassistNoInterfaceViewFactory#createView(java.lang.reflect.InvocationHandler, Class)
    */
   @Override
   public void bind() throws NamingException
   {
      if (this.endpointContext == null)
      {
         throw new IllegalStateException("KernelControllerContext hasn't been set for nointerface view binder of bean: " + this.beanClass);
      }
      final AsyncMethodsMetaData asyncMethods = this.sessionBeanMetaData.getAsyncMethods();
      InvocationHandler invocationHandler = new NoInterfaceViewInvocationHandler(this.endpointContext, null, beanClass,
            asyncMethods == null ? new AsyncMethodsMetaData() : asyncMethods);

      Object noInterfaceView;
      try
      {
         noInterfaceView = new JavassistProxyFactory().createProxy(new Class<?>[] {beanClass}, invocationHandler);
      }
      catch (Exception e)
      {
         throw new RuntimeException("Could not create no-interface view for bean class: " + beanClass, e);
      }
      this.prettyPrintJNDIBindingInfo();
      // bind to jndi
      NonSerializableFactory.rebind(this.jndiContext, this.noInterfaceViewJNDIName, noInterfaceView, true);
   }

   /**
    * Unbinds the no-interface view proxy from the JNDI
    * 
    * @see org.jboss.ejb3.nointerface.impl.jndi.NoInterfaceViewJNDIBinderFacade#unbindNoInterfaceView()
    */
   @Override
   public void unbind() throws NamingException
   {
      this.jndiContext.unbind(this.noInterfaceViewJNDIName);
   }
}
