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
package org.jboss.ejb3.nointerface.spi.view.factory;

import java.lang.reflect.InvocationHandler;

/**
 * NoInterfaceViewFactory
 *
 * Factory for creating no-interface view for a bean
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public interface NoInterfaceViewFactory
{

   /**
    * Creates a no-interface view for the EJB represented by the
    * <code>beanClass</code>
    *
    * @param <T>
    * @param invocationHandler The invocation handler responsible for handling 
    *                           requests on the no-interface view returned by this
    *                           method
    * @param beanClass The bean class (no validation on the Class is done to check for EJB semantics)
    * @return Returns the no-interface view for the <code>beanClass</code>
    * 
    * @throws Exception If any exceptions are encountered during the no-interface view creation
    */
   public <T> T createView(InvocationHandler invocationHandler, Class<T> beanClass) throws Exception;
}
