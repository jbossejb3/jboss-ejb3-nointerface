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
package org.jboss.ejb3.nointerface.integration.test.equality;

import javax.annotation.PostConstruct;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * EqualityCheckerBean
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
@Stateless
@Remote (EqualityCheckerBeanRemote.class)
public class EqualityCheckerBean implements EqualityCheckerBeanRemote
{
   
   private Context ctx;
   
   @PostConstruct
   public void onConstruct() throws Exception
   {
      this.ctx = new InitialContext();
   }

   /**
    * @see org.jboss.ejb3.nointerface.integration.test.equality.EqualityCheckerBeanRemote#checkEqualityOnDifferentJndiObjects(java.lang.String, java.lang.String)
    */
   @Override
   public boolean checkEqualityOnDifferentJndiObjects(String beanOneJndiName, String beanTwoJndiName)
   {
      try
      {
         Object beanOne = this.ctx.lookup(beanOneJndiName);
         Object beanTwo = this.ctx.lookup(beanTwoJndiName);
         return beanOne.equals(beanTwo);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * @see org.jboss.ejb3.nointerface.integration.test.equality.EqualityCheckerBeanRemote#checkEqualityOnMultipleInstances(java.lang.String)
    */
   @Override
   public boolean checkEqualityOnMultipleInstances(String jndiName)
   {
      try
      {
         Object instanceOne = this.ctx.lookup(jndiName);
         Object instanceTwo = this.ctx.lookup(jndiName);
         return instanceOne.equals(instanceTwo);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * @see org.jboss.ejb3.nointerface.integration.test.equality.EqualityCheckerBeanRemote#checkEqualityOnSameInstance(java.lang.String)
    */
   @Override
   public boolean checkEqualityOnSameInstance(String jndiName)
   {
      try
      {
         Object instanceOne = this.ctx.lookup(jndiName);
         return instanceOne.equals(instanceOne);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

}
