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
package org.jboss.ejb3.nointerface.integration.test.common;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * DelegatingBean
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
@Stateless
@Remote(DelegatingSLSBRemote.class)
public class DelegatingSLSBean implements DelegatingSLSBRemote
{

   /* (non-Javadoc)
    * @see org.jboss.ejb3.nointerface.integration.test.deployment.DelegatingSLSBRemote#echoFromBeanMarkedWithLocalBean(java.lang.String)
    */
   @Override
   public String echoFromBeanMarkedWithLocalBean(String msg)
   {
      // lookup nointerface bean
      Context ctx;
      try
      {
         ctx = new InitialContext();
         SLSBMarkedWithLocalBean bean = (SLSBMarkedWithLocalBean) ctx.lookup(SLSBMarkedWithLocalBean.JNDI_NAME);
         return bean.echo(msg);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.nointerface.integration.test.deployment.DelegatingSLSBRemote#echoFromNoInterfaceBeanWithInheritance(java.lang.String)
    */
   @Override
   public String echoFromNoInterfaceBeanWithInheritance(String msg)
   {
      // lookup nointerface bean
      Context ctx;
      try
      {
         ctx = new InitialContext();
         SLSBMarkedWithLocalBean bean = (SLSBMarkedWithLocalBean) ctx.lookup(ChildBean.JNDI_NAME);
         return bean.echo(msg);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.nointerface.integration.test.deployment.DelegatingSLSBRemote#echoFromSimpleNoInterfaceBean(java.lang.String)
    */
   @Override
   public String echoFromSimpleNoInterfaceBean(String msg)
   {
      // lookup nointerface bean
      Context ctx;
      try
      {
         ctx = new InitialContext();
         SimpleNoInterfaceSLSBean bean = (SimpleNoInterfaceSLSBean) ctx.lookup(SimpleNoInterfaceSLSBean.JNDI_NAME);
         return bean.echo(msg);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

}
