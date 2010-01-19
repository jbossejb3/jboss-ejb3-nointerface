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
package org.jboss.ejb3.nointerface.integration.test.equality.unit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.jboss.ejb3.nointerface.integration.test.AbstractNoInterfaceTestCase;
import org.jboss.ejb3.nointerface.integration.test.common.ChildBean;
import org.jboss.ejb3.nointerface.integration.test.common.CounterNoInterfaceSFSBean;
import org.jboss.ejb3.nointerface.integration.test.common.SLSBMarkedWithLocalBean;
import org.jboss.ejb3.nointerface.integration.test.common.SimpleNoInterfaceSFSB;
import org.jboss.ejb3.nointerface.integration.test.common.SimpleNoInterfaceSLSBean;
import org.jboss.ejb3.nointerface.integration.test.equality.EqualityCheckerBean;
import org.jboss.ejb3.nointerface.integration.test.equality.EqualityCheckerBeanRemote;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * EqualityTestCase
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class NoInterfaceBeanEqualityTestCase extends AbstractNoInterfaceTestCase
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
      String jarName = "nointerface-bean-equality.jar";
      deployment = buildSimpleJar(jarName, SimpleNoInterfaceSLSBean.class.getPackage(), EqualityCheckerBeanRemote.class
            .getPackage());
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
   public void testNoInterfaceSLSBEquality() throws Exception
   {
      EqualityCheckerBeanRemote delegateBean = (EqualityCheckerBeanRemote) this.getInitialContext().lookup(
            EqualityCheckerBean.class.getSimpleName() + "/remote");

      boolean areDifferentNoInterfaceSLSBEqual = delegateBean.checkEqualityOnDifferentJndiObjects(
            SimpleNoInterfaceSLSBean.JNDI_NAME, SLSBMarkedWithLocalBean.JNDI_NAME);
      assertFalse("Different nointerface SLSB type were considered equal", areDifferentNoInterfaceSLSBEqual);

      boolean areMultipleInstancesOfSameNoInterfaceSLSBEqual = delegateBean
            .checkEqualityOnMultipleInstances(ChildBean.JNDI_NAME);
      assertTrue("Multiple instances of same nointerface SLSB type were *not* considered equal ",
            areMultipleInstancesOfSameNoInterfaceSLSBEqual);

      boolean isSameInstanceOfNoInterfaceSLSBEqual = delegateBean
            .checkEqualityOnSameInstance(SimpleNoInterfaceSLSBean.JNDI_NAME);
      assertTrue("Same instance of a nointerface SLSB was *not* considered equal to itself ",
            isSameInstanceOfNoInterfaceSLSBEqual);

   }

   @Test
   public void testNoInterfaceSFSBEquality() throws Exception
   {
      EqualityCheckerBeanRemote delegateBean = (EqualityCheckerBeanRemote) this.getInitialContext().lookup(
            EqualityCheckerBean.class.getSimpleName() + "/remote");

      boolean areDifferentNoInterfaceSFSBEqual = delegateBean.checkEqualityOnDifferentJndiObjects(
            SimpleNoInterfaceSFSB.JNDI_NAME, CounterNoInterfaceSFSBean.JNDI_NAME);
      assertFalse("Different nointerface SFSB type were considered equal", areDifferentNoInterfaceSFSBEqual);

      boolean areMultipleInstancesOfSameNoInterfaceSFSBEqual = delegateBean
            .checkEqualityOnMultipleInstances(CounterNoInterfaceSFSBean.JNDI_NAME);
      assertFalse("Multiple instances of same nointerface SFSB type were considered equal ",
            areMultipleInstancesOfSameNoInterfaceSFSBEqual);

      boolean isSameInstanceOfNoInterfaceSFSBEqual = delegateBean
            .checkEqualityOnSameInstance(SimpleNoInterfaceSFSB.JNDI_NAME);
      assertTrue("Same instance of a nointerface SFSB was *not* considered equal to itself ",
            isSameInstanceOfNoInterfaceSFSBEqual);

   }

   
   @Test
   public void testNoInterfaceEqualityBetweenSFSBAndSLSB() throws Exception
   {
    
      EqualityCheckerBeanRemote delegateBean = (EqualityCheckerBeanRemote) this.getInitialContext().lookup(
            EqualityCheckerBean.class.getSimpleName() + "/remote");
      
      boolean isSFSBEqualToSLSB = delegateBean.checkEqualityOnDifferentJndiObjects(SimpleNoInterfaceSLSBean.JNDI_NAME,
            SimpleNoInterfaceSFSB.JNDI_NAME);
      assertFalse("An instance of a nointerface SFSB was considered equal to an instance of nointerface SLSB bean",
            isSFSBEqualToSLSB);
   }
}
