/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.ejb3.nointerface.test.factory.unit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.jboss.ejb3.nointerface.impl.view.factory.JavassistNoInterfaceViewFactory;
import org.jboss.ejb3.nointerface.spi.view.factory.NoInterfaceViewFactory;
import org.jboss.ejb3.nointerface.test.viewcreator.SimpleSFSBeanWithoutInterfaces;
import org.jboss.ejb3.nointerface.test.viewcreator.SimpleSLSBWithoutInterface;
import org.jboss.logging.Logger;
import org.junit.Test;

/**
 * NoInterfaceBeansTestCase
 *
 * Tests the no-inteface view for beans. 
 * 
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class NoInterfaceViewFactoryTestCase 
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(NoInterfaceViewFactoryTestCase.class);

   private class DummyInvocationHandler implements InvocationHandler
   {

      private Object target;
      
      public DummyInvocationHandler(Object target)
      {
         if (target == null)
         {
            throw new IllegalArgumentException("Target object cannot be null, for invocation handler");
         }
         this.target = target;
      }
      
      /**
       * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
       */
      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
      {
         // just pass on the call to the target
         return method.invoke(target, args);
      }
      
   }

   /**
    * Test to ensure that the no-interface view instance does NOT consider
    * a final method on the bean while creating the view
    *
    * @throws Exception
    */
   @Test
   public void testFinalMethodsAreNotConsideredInView() throws Exception
   {
      NoInterfaceViewFactory noInterfaceBeanFactory = new JavassistNoInterfaceViewFactory();
      SimpleSLSBWithoutInterface beanInstance = new SimpleSLSBWithoutInterface();
      Object proxy = noInterfaceBeanFactory.createView(new DummyInvocationHandler(beanInstance), beanInstance.getClass());
      // ensure that the returned object is not null and is of expected type
      assertNotNull("No-interface view factory returned a null view for " + beanInstance.getClass(), proxy);
      assertTrue("No-interface view factory returend an unexpected object type: " + proxy.getClass(), (proxy instanceof SimpleSLSBWithoutInterface));
      
      SimpleSLSBWithoutInterface noInterfaceView = (SimpleSLSBWithoutInterface) proxy;

      // Nothing fancy to test - just ensure that the declared methods in the proxy does
      // NOT contain a "final" method. Just check on method name, should be enough
      Method[] declaredMethods = noInterfaceView.getClass().getDeclaredMethods();
      for (Method declaredMethod : declaredMethods)
      {
         if (declaredMethod.getName().equals("someFinalMethod"))
         {
            fail("No-interface view has overriden a final method. It shouldn't have.");
         }
      }

   }

   /**
   * Test to ensure that the no-interface view instance does NOT consider
   * a static method on the bean while creating the view
   *
   * @throws Exception
   */
   @Test
   public void testStaticMethodsAreNotConsideredInView() throws Exception
   {
      NoInterfaceViewFactory noInterfaceBeanFactory = new JavassistNoInterfaceViewFactory();
      SimpleSFSBeanWithoutInterfaces beanInstance = new SimpleSFSBeanWithoutInterfaces();
      Object proxy = noInterfaceBeanFactory.createView(new DummyInvocationHandler(beanInstance), beanInstance.getClass());
      // ensure that the returned object is not null and is of expected type
      assertNotNull("No-interface view factory returned a null view for " + beanInstance.getClass(), proxy);
      assertTrue("No-interface view factory returend an unexpected object type: " + proxy.getClass(), (proxy instanceof SimpleSFSBeanWithoutInterfaces));
      
      SimpleSFSBeanWithoutInterfaces noInterfaceView = (SimpleSFSBeanWithoutInterfaces) proxy;
      

      // Nothing fancy to test - just ensure that the declared methods in the proxy does
      // NOT contain a "static" method. Just check on method name, should be enough
      Method[] declaredMethods = noInterfaceView.getClass().getDeclaredMethods();
      for (Method declaredMethod : declaredMethods)
      {
         if (declaredMethod.getName().equals("someStaticMethod"))
         {
            fail("No-interface view has overriden a static method. It shouldn't have.");
         }
      }
   }

//   /**
//    * Test to ensure that a no-interface view is NOT created for session beans
//    * which implement an interface (and do not explicitly mark themselves @LocalBean)
//    *
//    * @throws Exception
//    */
//   @Test
//   public void testBeanWithInterfacesIsNotEligibleForNoInterfaceView() throws Exception
//   {
//      JBossSessionBeanMetaData slsbMetadata = MetaDataHelper
//            .getMetadataFromBeanImplClass(StatelessBeanWithInterfaces.class);
//      String slsbNoInterfaceViewJNDIName = slsbMetadata.getEjbName() + "/no-interface";
//
//      JBossSessionBeanMetaData sfsbMetadata = MetaDataHelper
//            .getMetadataFromBeanImplClass(StatefulBeanWithInterfaces.class);
//      String sfsbNoInterfaceViewJNDIName = sfsbMetadata.getEjbName() + "/no-interface";
//      String sfsbNoInterfaceViewFactoryJNDIName = sfsbMetadata.getEjbName() + "/no-interface-stateful-proxyfactory";
//
//      Context ctx = new InitialContext();
//      // we have to ensure that there is NO no-interface view for these beans (because they are not eligible)
//      try
//      {
//         Object obj = ctx.lookup(slsbNoInterfaceViewJNDIName);
//         // this is a failure because there should not be a no-interface view for these beans
//         fail("A SLSB with interfaces was marked as eligible for no-interface view. Shouldn't have been. Found object of type "
//               + obj.getClass() + " in the jndi for jndiname " + slsbNoInterfaceViewJNDIName);
//      }
//      catch (NameNotFoundException nnfe)
//      {
//         // expected
//      }
//
//      // now for sfsb, test that neither the factory nor the view are NOT bound
//
//      // test factory binding
//      try
//      {
//         Object obj = ctx.lookup(sfsbNoInterfaceViewFactoryJNDIName);
//         // this is a failure because there should not be a no-interface view for these beans
//         fail("A SFSB factory for no-interface view was created for a bean implementing interfaces. Shouldn't have been. Found object of type "
//               + obj.getClass() + " in the jndi for jndiname " + sfsbNoInterfaceViewFactoryJNDIName);
//      }
//      catch (NameNotFoundException nnfe)
//      {
//         // expected
//      }
//      // sfsb no-interface view
//      try
//      {
//         Object obj = ctx.lookup(sfsbNoInterfaceViewJNDIName);
//         // this is a failure because there should not be a no-interface view for these beans
//         fail("A no-interface view for SFSB was created for a bean implementing interfaces. Shouldn't have been. Found object of type "
//               + obj.getClass() + " in the jndi for jndiname " + sfsbNoInterfaceViewJNDIName);
//      }
//      catch (NameNotFoundException nnfe)
//      {
//         // expected
//      }
//
//   }
//
//   /**
//    * Test that sessions are created as expected for stateful session beans
//    *
//    * @throws Exception
//    */
//   @Test
//   public void testSessionCreationForSFSBNoInterfaceViews() throws Exception
//   {
//      JBossSessionBeanMetaData sessionBeanMetadata = MetaDataHelper
//            .getMetadataFromBeanImplClass(SimpleSFSBeanWithoutInterfaces.class);
//      String jndiName = sessionBeanMetadata.getEjbName() + "/no-interface";
//
//      Context ctx = new InitialContext();
//      // let's assume the lookup returns the correct type.
//      // there are other test cases to ensure it does return the correct type
//      SimpleSFSBeanWithoutInterfaces firstSFSB = (SimpleSFSBeanWithoutInterfaces) ctx.lookup(jndiName);
//      // ensure this is a clean bean
//      int initQty = firstSFSB.getQtyPurchased();
//      assertEquals("SFSB instance is not new", initQty, SimpleSFSBeanWithoutInterfaces.INITIAL_QTY);
//      // now change the state of the sfsb instance
//      firstSFSB.incrementPurchaseQty();
//      int incrementedValueForFirstSFSB = firstSFSB.getQtyPurchased();
//      assertEquals("SFSB instance's value not incremented", incrementedValueForFirstSFSB,
//            SimpleSFSBeanWithoutInterfaces.INITIAL_QTY + 1);
//
//      // now lookup another bean
//      SimpleSFSBeanWithoutInterfaces secondSFSB = (SimpleSFSBeanWithoutInterfaces) ctx.lookup(jndiName);
//      // ensure this is a clean bean
//      int initQtyForSecondBeanInstance = secondSFSB.getQtyPurchased();
//      assertEquals("Second instance of SFSB is not new", initQtyForSecondBeanInstance,
//            SimpleSFSBeanWithoutInterfaces.INITIAL_QTY);
//      // now change the state of the sfsb instance by some x amount
//      int incrementBy = 10;
//      secondSFSB.incrementPurchaseQty(incrementBy);
//      int incrementedValueForSecondSFSB = secondSFSB.getQtyPurchased();
//      assertEquals("Second SFSB instance's value not incremented", incrementedValueForSecondSFSB,
//            SimpleSFSBeanWithoutInterfaces.INITIAL_QTY + incrementBy);
//
//      // let's also (again) check that the first SFSB still has it's own values and hasn't been
//      // affected by changes made to second SFSB
//      assertEquals("Value in first SFSB was changed when second SFSB was being modified", incrementedValueForFirstSFSB,
//            SimpleSFSBeanWithoutInterfaces.INITIAL_QTY + 1);
//
//      // also check equality of two sfsb instances - they should not be equal
//      assertFalse("Both the instances of the SFSB are the same", firstSFSB.equals(secondSFSB));
//
//      // let's also check whether the bean is equal to itself
//      assertTrue("Incorrect equals implementation - returns false for the same sfsb instance", firstSFSB
//            .equals(firstSFSB));
//      assertTrue("equals returned false for the same sfsb instance", secondSFSB.equals(secondSFSB));
//
//   }

}
