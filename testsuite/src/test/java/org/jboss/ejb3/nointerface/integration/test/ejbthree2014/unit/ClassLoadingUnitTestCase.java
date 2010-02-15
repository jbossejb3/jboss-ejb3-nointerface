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
package org.jboss.ejb3.nointerface.integration.test.ejbthree2014.unit;

import java.io.File;
import java.net.URL;

import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.Assert;

import org.jboss.ejb3.nointerface.integration.test.AbstractNoInterfaceTestCase;
import org.jboss.ejb3.nointerface.integration.test.ejbthree2014.ContactManager;
import org.jboss.ejb3.nointerface.integration.test.ejbthree2014.WrapperBean;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the fix for https://jira.jboss.org/jira/browse/EJBTHREE-2014
 * 
 * <p>
 *  Loading of custom class objects being passed through methods of a nointerface view
 *  bean were not being handled correctly, which was resulting in a {@link ClassNotFoundException}.
 *       
 * </p>
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class ClassLoadingUnitTestCase extends AbstractNoInterfaceTestCase
{

   

   private URL deployment;

   private final String JAR_NAME = "classloading-test.jar";

   /**
    * 
    * @return
    * @throws Exception
    */
   @Before
   public void before() throws Exception
   {
      File jar = buildSimpleJar(JAR_NAME, WrapperBean.class.getPackage());
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

   /**
    * Test that a nointerface view bean which accepts a custom class object (i.e. not classes not 
    * belonging to the rt.jar) is able to process invocations successfully.
    * 
    * @throws Exception
    */
   @Test
   public void testInvocationWithCustomClassMethodParam() throws Exception
   {
      Context ctx = new InitialContext();
      ContactManager contactManager = (ContactManager) ctx.lookup(WrapperBean.JNDI_NAME);
      String contactname = "somename";
      String alias = contactManager.getContactAlias(contactname);

      Assert.assertNotNull("Alias returned for contact name " + contactname + " is null", alias);
      Assert.assertEquals("Unexpected alias returned", contactname + "-alias", alias);

   }
}
