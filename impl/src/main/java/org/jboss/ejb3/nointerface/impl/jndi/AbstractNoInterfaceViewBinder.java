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
package org.jboss.ejb3.nointerface.impl.jndi;

import javax.naming.Context;
import javax.naming.NamingException;

import org.jboss.ejb3.nointerface.impl.invocationhandler.NoInterfaceViewInvocationHandler;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossSessionBean31MetaData;

/**
 * AbstractNoInterfaceViewJNDIBinder
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public abstract class AbstractNoInterfaceViewBinder 
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(AbstractNoInterfaceViewBinder.class);
   
   /**
    * The endpoint context which will be used while creating the {@link NoInterfaceViewInvocationHandler}
    */
   protected KernelControllerContext endpointContext;
   
   protected Class<?> beanClass;
   
   protected JBossSessionBean31MetaData sessionBeanMetaData;
   
   protected Context jndiContext;
   
   protected String noInterfaceViewJNDIName;
   
   /**
    * Creates a jndi binder 
    * @param endPointCtx The endpoint context which will be used by the {@link NoInterfaceViewInvocationHandler}
    */
   public AbstractNoInterfaceViewBinder(Context jndiCtx, String jndiName, Class<?> beanClass, JBossSessionBean31MetaData beanMetaData)
   {
      if (!beanMetaData.isNoInterfaceBean())
      {
         throw new IllegalStateException(beanMetaData.getEjbName() + " bean does not have a no-interface view");
      }
      this.beanClass = beanClass;
      this.noInterfaceViewJNDIName = jndiName;
      this.sessionBeanMetaData = beanMetaData;
      this.jndiContext = jndiCtx;
   }
   
   public void start() throws Exception
   {
      this.bind();
   }
   
   public void stop() throws Exception
   {
      this.unbind();
   }
   
   protected abstract void bind() throws NamingException;
   
   protected abstract void unbind() throws NamingException;
   
   /**
    * Utility method to log the jndi name, to which the no-interface view of the bean represented
    * by the <code>sessionBean<code>, will be bound.
    * 
    */
   protected void prettyPrintJNDIBindingInfo()
   {
      StringBuffer sb = new StringBuffer();
      sb.append("Binding the following entry in Global JNDI for bean:" + this.sessionBeanMetaData.getEjbName() + "\n\n");
      sb.append("\t");
      sb.append(this.noInterfaceViewJNDIName);
      sb.append(" -> EJB3.1 no-interface view\n");
      
      logger.info(sb);
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
