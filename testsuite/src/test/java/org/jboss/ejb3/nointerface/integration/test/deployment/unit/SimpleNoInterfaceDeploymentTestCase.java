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
package org.jboss.ejb3.nointerface.integration.test.deployment.unit;

import static org.junit.Assert.assertEquals;

import java.net.URL;

import org.jboss.ejb3.nointerface.integration.test.AbstractNoInterfaceTestCase;
import org.jboss.ejb3.nointerface.integration.test.common.DelegatingSFSBRemote;
import org.jboss.ejb3.nointerface.integration.test.common.DelegatingSFSBean;
import org.jboss.ejb3.nointerface.integration.test.common.DelegatingSLSBRemote;
import org.jboss.ejb3.nointerface.integration.test.common.DelegatingSLSBean;
import org.jboss.ejb3.nointerface.integration.test.common.SimpleNoInterfaceSLSBean;
import org.jboss.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * SimpleNoInterfaceDeploymentTestCase
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class SimpleNoInterfaceDeploymentTestCase extends AbstractNoInterfaceTestCase
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(SimpleNoInterfaceDeploymentTestCase.class);

   private URL deployment;

   /**
    * 
    * @return
    * @throws Exception
    */
   @Before
   public void before() throws Exception
   {
      String jarName = "simple-nointerface-beans.jar";
      deployment = buildSimpleJar(jarName, SimpleNoInterfaceSLSBean.class.getPackage());
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
      DelegatingSLSBRemote delegateBean = (DelegatingSLSBRemote) this.getInitialContext().lookup(
            DelegatingSLSBean.class.getSimpleName() + "/remote");
      String msg = "Some message!";
      assertEquals("Unexpected message returned by bean", msg, delegateBean.echoFromSimpleNoInterfaceBean(msg));
   }

   /**
    * 
    * @throws Exception
    */
   @Test
   public void testNoInterfaceSFSBAccess() throws Exception
   {
      DelegatingSFSBRemote delegateSFSBean = (DelegatingSFSBRemote) this.getInitialContext().lookup(
            DelegatingSFSBean.class.getSimpleName() + "/remote");

      assertEquals("Initial count returned by the bean isn't zero", 0, delegateSFSBean.getCount());
      // increment once
      delegateSFSBean.incrementCount();
      assertEquals("Bean returned unexpected count", 1, delegateSFSBean.getCount());
      int prevCount = delegateSFSBean.getCount();
      // increment n number of times
      final int NUM_TIMES = 8;
      for (int i = 0; i < NUM_TIMES; i++)
      {
         delegateSFSBean.incrementCount();
      }
      assertEquals("Bean returned unexpected count", prevCount + NUM_TIMES, delegateSFSBean.getCount());
   }

   @Test
   public void testLocalBeanWithInterfaces() throws Exception
   {
      DelegatingSLSBRemote delegateBean = (DelegatingSLSBRemote) this.getInitialContext().lookup(
            DelegatingSLSBean.class.getSimpleName() + "/remote");
      String msg = "Some other message!";
      assertEquals("Unexpected message returned by bean", msg, delegateBean.echoFromBeanMarkedWithLocalBean(msg));
   }
}
