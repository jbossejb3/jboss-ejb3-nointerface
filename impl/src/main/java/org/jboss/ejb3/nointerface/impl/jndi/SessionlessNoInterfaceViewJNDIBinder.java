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
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossSessionBean31MetaData;
import org.jboss.util.naming.NonSerializableFactory;

import static org.jboss.ejb3.nointerface.impl.jndi.JNDINameUtil.resolveNoInterfaceJNDIName;

/**
 * Responsible for binding a nointerface view proxy into jndi, for
 * EJBs which are *not* session aware (ex: Stateless beans and Singleton beans)
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class SessionlessNoInterfaceViewJNDIBinder extends AbstractNoInterfaceViewJNDIBinder
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(SessionlessNoInterfaceViewJNDIBinder.class);

   /**
    * Constructor
    * 
    * @param ctx
    * @param beanClass
    * @param sessionBeanMetadata
    */
   public SessionlessNoInterfaceViewJNDIBinder(KernelControllerContext endPointCtx)
   {
      super(endPointCtx);
   }

   /**
    * Creates the no-interface view for the bean and binds it to the JNDI
    * under the no-interface view jndi name obtained from <code>sessionBeanMetadata</code>.
    *
    * @see JavassistNoInterfaceViewFactory#createView(java.lang.reflect.InvocationHandler, Class)
    */
   @Override
   public String bindNoInterfaceView(Context jndiCtx, Class<?> beanClass, JBossSessionBean31MetaData beanMetaData)
         throws NamingException, IllegalStateException
   {
      // ensure no-interface view exists
      this.ensureNoInterfaceViewExists(beanMetaData);


      InvocationHandler invocationHandler = new NoInterfaceViewInvocationHandler(this.endpointContext, null, beanClass);

      Object noInterfaceView;
      try
      {
         noInterfaceView = new JavassistProxyFactory().createProxy(new Class<?>[] {beanClass}, invocationHandler);
      }
      catch (Exception e)
      {
         throw new RuntimeException("Could not create no-interface view for bean class: " + beanClass, e);
      }
      // get no-interface view jndi name
      String noInterfaceJndiName = resolveNoInterfaceJNDIName(beanMetaData);
      // log the no-interface view jndi binding info
      this.prettyPrintJNDIBindingInfo(beanMetaData, noInterfaceJndiName);
      // bind to jndi
      NonSerializableFactory.rebind(jndiCtx, noInterfaceJndiName, noInterfaceView, true);
      
      return noInterfaceJndiName;
   }

   /**
    * Unbinds the no-interface view proxy from the JNDI
    * 
    * @see org.jboss.ejb3.nointerface.impl.jndi.NoInterfaceViewJNDIBinderFacade#unbindNoInterfaceView()
    */
   @Override
   public void unbindNoInterfaceView(Context jndiCtx, Class<?> beanClass, JBossSessionBean31MetaData beanMetaData)
         throws NamingException, IllegalStateException
   {
      // ensure no-interface view exists
      this.ensureNoInterfaceViewExists(beanMetaData);

      String noInterfaceJndiName = resolveNoInterfaceJNDIName(beanMetaData);
      jndiCtx.unbind(noInterfaceJndiName);

   }
}
