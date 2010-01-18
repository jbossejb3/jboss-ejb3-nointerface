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
package org.jboss.ejb3.nointerface.spi.jndi;

import javax.naming.Context;
import javax.naming.NamingException;

import org.jboss.metadata.ejb.jboss.JBossSessionBean31MetaData;

/**
 * NoInterfaceViewJNDIBinder
 *
 * Responsible for binding and unbinding no-interface view(s) to/from the JNDI.
 * 
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public interface NoInterfaceViewJNDIBinder
{

   /**
    * Binds the no-interface view of the bean <code>beanClass</code> to the JNDI,
    * at the provided <code>jndiCtx</code> context.
    * 
    * @param jndiCtx The jndi context to which the no-interface view has to be bound
    * @param beanClass The EJB class
    * @param beanMetaData The metadata of the bean
    * 
    * @return Returns the jndi-name where the no-interface view has been bound
    * @throws NamingException If any exception while binding to JNDI
    * @throws IllegalStateException If a no-interface view is not applicable for this bean
    */
   String bindNoInterfaceView(Context jndiCtx, Class<?> beanClass, JBossSessionBean31MetaData beanMetaData)
         throws NamingException, IllegalStateException;

   /**
    * Unbind the no-interface view of the bean <code>beanClass</code> from the JNDI
    * at the provided <code>jndiCtx</code> context.
    * 
    * @param jndiCtx The jndi context from where the no-interface view has to be unbound
    * @param beanClass The EJB class
    * @param beanMetaData The metadata of the bean
    * 
    * @throws NamingException If any exception while unbinding from JNDI
    * @throws IllegalStateException If a no-interface view is not applicable for this bean 
    */
   void unbindNoInterfaceView(Context jndiCtx, Class<?> beanClass, JBossSessionBean31MetaData beanMetaData)
         throws NamingException, IllegalStateException;
}
