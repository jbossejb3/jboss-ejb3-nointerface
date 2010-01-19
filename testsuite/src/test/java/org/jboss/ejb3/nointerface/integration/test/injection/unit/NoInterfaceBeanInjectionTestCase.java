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
package org.jboss.ejb3.nointerface.integration.test.injection.unit;

import static org.junit.Assert.assertEquals;

import java.net.URL;

import org.jboss.ejb3.nointerface.integration.test.AbstractNoInterfaceTestCase;
import org.jboss.ejb3.nointerface.integration.test.injection.Calculator;
import org.jboss.ejb3.nointerface.integration.test.injection.CalculatorBean;
import org.jboss.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * NoInterfaceBeanInjectionTestCase
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class NoInterfaceBeanInjectionTestCase extends AbstractNoInterfaceTestCase
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(NoInterfaceBeanInjectionTestCase.class);

   private URL deployment;

   /**
    * 
    * @return
    * @throws Exception
    */
   @Before
   public void before() throws Exception
   {
      String jarName = NoInterfaceBeanInjectionTestCase.class.getSimpleName() + ".jar";
      deployment = buildSimpleJar(jarName, CalculatorBean.class.getPackage());
      this.redeploy(deployment);
   }

   @After
   public void after() throws Exception
   {
      if (this.deployment != null)
      {
         this.undeploy(deployment);
      }
   }

   /**
    * 
    * @throws Exception
    */
   @Test
   public void testNoInterfaceSLSBAccess() throws Exception
   {
      Calculator calculatorDelegatingBean = (Calculator) this.getInitialContext().lookup(CalculatorBean.REMOTE_JNDI_NAME);
      int num1 = 5;
      int num2 = 6;
      int expectedResult = num1 + num2;
      int result = calculatorDelegatingBean.add(num1, num2);
      assertEquals("Unexpected result from calculator", expectedResult, result);
   }

}
