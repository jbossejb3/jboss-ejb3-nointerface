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

import java.io.File;
import java.net.URL;

import org.jboss.ejb3.nointerface.integration.test.AbstractNoInterfaceTestCase;
import org.jboss.ejb3.nointerface.integration.test.deployment.AccountManager;
import org.jboss.ejb3.nointerface.integration.test.deployment.AccountManagerBean;
import org.jboss.ejb3.nointerface.integration.test.deployment.Echo;
import org.jboss.ejb3.nointerface.integration.test.deployment.OuterSFSB;
import org.jboss.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * EarDeploymentUnitTestCase
 * 
 * Tests the deployment of no-interface view beans through a .ear file
 * 
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class EarDeploymentUnitTestCase extends AbstractNoInterfaceTestCase
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(EarDeploymentUnitTestCase.class);

   private URL deployment;

   private final String EAR_FILE_NAME_PREFIX = "ear-deployment-test";

   private static final File TARGET_DIRECTORY = new File(BASEDIR, "target");
   
   /**
    * 
    * @return
    * @throws Exception
    */
   @Before
   public void before() throws Exception
   {
      String jarName = "ejb31-beans.jar";
      File jar = buildSimpleJar(jarName, AccountManager.class.getPackage());
      File ear = buildSimpleEAR(EAR_FILE_NAME_PREFIX + ".ear", jar);
      this.deployment = ear.toURI().toURL();
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
    * Test that a SLSB with a no-interface view, deployed through an .ear 
    * deploys and works fine
    *  
    * @throws Exception
    */
   @Test
   public void testNoInterfaceSLSBAccess() throws Exception
   {
      String jndiName = EAR_FILE_NAME_PREFIX + "/" + AccountManagerBean.class.getSimpleName() + "/remote";
      AccountManager accountManager = (AccountManager) this.getInitialContext().lookup(jndiName);

      int initialBalance = accountManager.getBalance();
      // credit amount
      int balance = accountManager.credit(100);
      assertEquals("Bean returned unexpected balance amount", initialBalance + 100, balance);
   }

   /**
    * Test that a SFSB with a no-interface view, deployed through an .ear 
    * deploys and works fine
    * 
    * @throws Exception
    */
   @Test
   public void testNoInterfaceSFSBAccess() throws Exception
   {
      String jndiName = EAR_FILE_NAME_PREFIX + "/" + OuterSFSB.class.getSimpleName() + "/remote";
      Echo echoBean = (Echo) this.getInitialContext().lookup(jndiName);
      String msg = "Some message!";
      assertEquals("Unexpected message returned by bean", msg, echoBean.echo(msg));
   }
}
