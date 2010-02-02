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
package org.jboss.ejb3.nointerface.integration.test.descriptor.unit;

import java.io.File;
import java.net.URL;

import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.Assert;

import org.jboss.ejb3.nointerface.integration.test.AbstractNoInterfaceTestCase;
import org.jboss.ejb3.nointerface.integration.test.descriptor.DDBasedNoInterfaceViewSLSB;
import org.jboss.ejb3.nointerface.integration.test.descriptor.NamedBean;
import org.jboss.ejb3.nointerface.integration.test.descriptor.WrapperSLSB;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * DeploymentDescriptorBasedTestCase
 * 
 * Tests the nointerface view for deployment descriptor based
 * deployments
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class DeploymentDescriptorBasedTestCase extends AbstractNoInterfaceTestCase
{

   /**
    * Test deployment which will be deployed to the AS
    */
   private URL deployment;

   /**
    * 
    * @return
    * @throws Exception
    */
   @Before
   public void before() throws Exception
   {
      String jarName = "dd-based-nointerface.jar";
      File jar = buildSimpleJar(jarName, DDBasedNoInterfaceViewSLSB.class.getPackage());
      this.deployment = jar.toURI().toURL();
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
   
   @Test
   public void testNoInterfaceViewDeployment() throws Exception
   {
      Context ctx = new InitialContext();
      NamedBean wrapperSLSB = (NamedBean) ctx.lookup(WrapperSLSB.JNDI_NAME);
      
      Assert.assertNotNull("lookup returned null for jndi name " + WrapperSLSB.JNDI_NAME, wrapperSLSB);
      
      String nameOfNoInterfaceViewSLSB = wrapperSLSB.getName();
      Assert.assertEquals("Incorrect name returned", DDBasedNoInterfaceViewSLSB.class.getSimpleName(), nameOfNoInterfaceViewSLSB);
      
      
   }
}
