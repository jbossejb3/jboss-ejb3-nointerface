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

import java.lang.reflect.InvocationHandler;

import javassist.util.proxy.ProxyFactory;

import org.jboss.ejb3.nointerface.impl.view.JavassistInvocationHandlerAdapter;
import org.jboss.ejb3.nointerface.impl.view.NoInterfaceViewMethodFilter;
import org.jboss.ejb3.nointerface.spi.view.factory.NoInterfaceViewFactory;
import org.jboss.logging.Logger;

/**
 * {@link JavassistNoInterfaceViewFactory} uses Javassist to create
 * nointerface view proxies for a EJB which exposes the nointerface view
 *
 * 
 * @see NoInterfaceViewFactory
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class JavassistNoInterfaceViewFactory implements NoInterfaceViewFactory
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(JavassistNoInterfaceViewFactory.class);

   /**
    * Inspects the bean class for all public methods and creates a proxy (sub-class)
    * out of it with overriden implementation of the public methods. The overriden
    * implementation will just give a call to <code>container</code>'s invoke(...) method
    * which handles the actual call.
    *
    * @param <T>
    * @param invocationHandler The container correpsonding to the bean class
    * @param beanClass The bean class (currently assumed)
    * @return Returns the no-interface view for the <code>beanClass</code>
    * @throws Exception
    */
   public <T> T createView(InvocationHandler invocationHandler, Class<T> beanClass) throws Exception
   {
      if (logger.isTraceEnabled())
      {
         logger.trace("Creating nointerface view for beanClass: " + beanClass + " with container " + invocationHandler);
      }

      ProxyFactory javassistProxyFactory = new ProxyFactory();
      // set the bean class for which we need a proxy
      javassistProxyFactory.setSuperclass(beanClass);
      // set a method filter so that we can filter out invocations on methods
      // which are *not* to be handled by a nointerface view 
      javassistProxyFactory.setFilter(new NoInterfaceViewMethodFilter());
      // Set our method handler which is responsible for handling the method invocations
      // on the proxy
      javassistProxyFactory.setHandler(new JavassistInvocationHandlerAdapter(invocationHandler));

      // create the proxy
      Object proxy = javassistProxyFactory.create(new Class[0], new Object[0]);
      // cast to the bean type and return
      return beanClass.cast(proxy);

   }

}
