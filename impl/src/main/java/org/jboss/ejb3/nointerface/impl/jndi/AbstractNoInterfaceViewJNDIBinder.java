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

import org.jboss.ejb3.nointerface.impl.invocationhandler.NoInterfaceViewInvocationHandler;
import org.jboss.ejb3.nointerface.spi.jndi.NoInterfaceViewJNDIBinder;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossSessionBean31MetaData;
import org.jboss.metadata.ejb.jboss.jndi.resolver.impl.JNDIPolicyBasedJNDINameResolverFactory;
import org.jboss.metadata.ejb.jboss.jndi.resolver.spi.SessionBean31JNDINameResolver;
import org.jboss.metadata.ejb.jboss.jndipolicy.plugins.DefaultJNDIBindingPolicyFactory;
import org.jboss.metadata.ejb.jboss.jndipolicy.spi.DefaultJndiBindingPolicy;

/**
 * AbstractNoInterfaceViewJNDIBinder
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public abstract class AbstractNoInterfaceViewJNDIBinder implements NoInterfaceViewJNDIBinder
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(AbstractNoInterfaceViewJNDIBinder.class);
   
   /**
    * The endpoint context which will be used while creating the {@link NoInterfaceViewInvocationHandler}
    */
   protected KernelControllerContext endpointContext;
   
   /**
    * Creates a jndi binder 
    * @param endPointCtx The endpoint context which will be used by the {@link NoInterfaceViewInvocationHandler}
    */
   public AbstractNoInterfaceViewJNDIBinder(KernelControllerContext endPointCtx)
   {
      if (endPointCtx == null)
      {
         throw new IllegalArgumentException("Endpoint kernel controller context cannot be null while creating a "
               + this.getClass().getSimpleName());
      }
      this.endpointContext = endPointCtx;
   }
   
   /**
    * Ensures that the bean represented by the <code>beanMetadata</code> has a no-interface view.
    * If a no-interface view is not present, then it throws a {@link IllegalStateException}
    * 
    * @param beanMetadata The bean metadata
    */
   protected void ensureNoInterfaceViewExists(JBossSessionBean31MetaData beanMetadata) throws IllegalStateException
   {
      if (!beanMetadata.isNoInterfaceBean())
      {
         throw new IllegalStateException(beanMetadata.getEjbName() + " bean does not have a no-interface view");
      }
   }
   
   /**
    * @param sessionBean The session bean metadata
    * @return Returns a jndi-name resolver for the session bean
    */
   protected SessionBean31JNDINameResolver getJNDINameResolver(JBossSessionBean31MetaData sessionBean)
   {
      DefaultJndiBindingPolicy jndiBindingPolicy = DefaultJNDIBindingPolicyFactory.getDefaultJNDIBindingPolicy();
      return JNDIPolicyBasedJNDINameResolverFactory.getJNDINameResolver(sessionBean, jndiBindingPolicy);
   }

   /**
    * Utility method to log the jndi name, to which the no-interface view of the bean represented
    * by the <code>sessionBean<code>, will be bound.
    * 
    * @param sessionBeanMetaData Session bean metadata
    * @param noInterfaceViewJNDIName The jndi name to which the no-interface view will be bound
    */
   protected void prettyPrintJNDIBindingInfo(JBossSessionBean31MetaData sessionBeanMetaData, String noInterfaceViewJNDIName)
   {
      StringBuffer sb = new StringBuffer();
      sb.append("Binding the following entry in Global JNDI:\n\n");
      sb.append("\t");
      sb.append(noInterfaceViewJNDIName);
      sb.append(" - EJB3.1 no-interface view\n");
      
      logger.info(sb);
   }
}
