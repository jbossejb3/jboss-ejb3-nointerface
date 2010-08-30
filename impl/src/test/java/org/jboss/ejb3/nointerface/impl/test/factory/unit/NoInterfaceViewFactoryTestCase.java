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
package org.jboss.ejb3.nointerface.impl.test.factory.unit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import junit.framework.Assert;

import org.jboss.dependency.spi.ControllerState;
import org.jboss.ejb3.endpoint.Endpoint;
import org.jboss.ejb3.endpoint.SessionFactory;
import org.jboss.ejb3.nointerface.impl.jndi.SessionAwareNoInterfaceViewJNDIBinder;
import org.jboss.ejb3.nointerface.impl.jndi.SessionlessNoInterfaceViewJNDIBinder;
import org.jboss.ejb3.nointerface.impl.test.factory.GrandChildSFSB;
import org.jboss.ejb3.nointerface.impl.test.factory.GrandChildSLSB;
import org.jboss.ejb3.nointerface.impl.test.factory.SimpleSFSBeanWithoutInterfaces;
import org.jboss.ejb3.nointerface.impl.test.factory.SimpleSLSBWithoutInterface;
import org.jboss.ejb3.proxy.javassist.JavassistProxyFactory;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.jboss.logging.Logger;
import org.jboss.metadata.annotation.creator.ejb.jboss.JBoss50Creator;
import org.jboss.metadata.annotation.finder.AnnotationFinder;
import org.jboss.metadata.annotation.finder.DefaultAnnotationFinder;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBean31MetaData;
import org.jboss.naming.JavaCompInitializer;
import org.jnp.server.SingletonNamingServer;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;

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

   private static JavaCompInitializer javaCompInitializer;

   @BeforeClass
   public static void beforeClass() throws Exception
   {
      bootupNamingServer();
   }

   private static void bootupNamingServer() throws Exception
   {
      SingletonNamingServer namingServer = new SingletonNamingServer();

      javaCompInitializer = new JavaCompInitializer();
      javaCompInitializer.start();
   }

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
      JavassistProxyFactory proxyFactory = new JavassistProxyFactory();
      SimpleSLSBWithoutInterface beanInstance = new SimpleSLSBWithoutInterface();
      Object proxy = proxyFactory.createProxy(new Class<?>[]
      {SimpleSLSBWithoutInterface.class}, new DummyInvocationHandler(beanInstance));
      // ensure that the returned object is not null and is of expected type
      assertNotNull("No-interface view factory returned a null view for " + beanInstance.getClass(), proxy);
      assertTrue("No-interface view factory returend an unexpected object type: " + proxy.getClass(),
            (proxy instanceof SimpleSLSBWithoutInterface));

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
      JavassistProxyFactory proxyFactory = new JavassistProxyFactory();
      SimpleSFSBeanWithoutInterfaces beanInstance = new SimpleSFSBeanWithoutInterfaces();
      Object proxy = proxyFactory.createProxy(new Class<?>[]
      {SimpleSFSBeanWithoutInterfaces.class}, new DummyInvocationHandler(beanInstance));
      // ensure that the returned object is not null and is of expected type
      assertNotNull("No-interface view factory returned a null view for " + beanInstance.getClass(), proxy);
      assertTrue("No-interface view factory returend an unexpected object type: " + proxy.getClass(),
            (proxy instanceof SimpleSFSBeanWithoutInterfaces));

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

   /**
    * Tests that the no-interface view created for a SLSB is type-compatible with the bean class
    * 
    * @throws Exception
    */
   @Test
   public void testSLSBProxyType() throws Exception
   {
      // mock a kernel controller context
      KernelControllerContext mockKernelControllerCtx = mock(KernelControllerContext.class);
      // create the SLSB nointerface view binder
      SessionlessNoInterfaceViewJNDIBinder jndiBinder = new SessionlessNoInterfaceViewJNDIBinder(
            mockKernelControllerCtx);
      Context jndiCtx = this.getJNDIContext();
      
      // create metadata for the SLSB
      JBossMetaData metadata = this.createMetaData(GrandChildSLSB.class);
      JBossSessionBean31MetaData sessionBean = (JBossSessionBean31MetaData) metadata.getEnterpriseBean(GrandChildSLSB.class
            .getSimpleName());
      
      // bind
      jndiBinder.bindNoInterfaceView(jndiCtx, GrandChildSLSB.class, sessionBean);

      // now lookup the bound no-interface view and carry out the type compatible tests
      String jndiName = GrandChildSLSB.class.getSimpleName() + "/no-interface";
      Object noInterfaceView = jndiCtx.lookup(jndiName);

      Assert.assertNotNull("Lookup of nointerface view returned null", noInterfaceView);

      Assert.assertTrue("no-interface view is not of type: " + GrandChildSLSB.class, noInterfaceView instanceof GrandChildSLSB);

      Class<?> superClass = GrandChildSLSB.class.getSuperclass();
      while (superClass != null)
      {
         Assert.assertTrue("no-interface view is not type-compatible, can't be cast to: " + superClass, superClass
               .isInstance(noInterfaceView));
         Class<?> interfaces[] = superClass.getInterfaces();
         for (Class<?> interfaceType : interfaces)
         {
            Assert.assertTrue("no-interface view is not type-compatible, can't be cast to: " + interfaceType,
                  interfaceType.isInstance(noInterfaceView));
         }
         superClass = superClass.getSuperclass();
      }

   }
   
   /**
    * Tests that the no-interface view created for a SFSB is type-compatible with the bean class
    * 
    * @throws Exception
    */
   @Test
   public void testSFSBProxyType() throws Exception
   {
      // set up the mocks required for the SFSB no-interface view jndi binder
      KernelControllerContext mockKernelControllerCtx = mock(KernelControllerContext.class);
      KernelController mockKernelController = mock(KernelController.class);
      when(mockKernelControllerCtx.getController()).thenReturn(mockKernelController);
      when(mockKernelControllerCtx.getState()).thenReturn(ControllerState.INSTALLED);
      // mock endpoint
      Endpoint mockEndPoint = mock(Endpoint.class);
      when(mockKernelControllerCtx.getTarget()).thenReturn(mockEndPoint);
      when(mockEndPoint.isSessionAware()).thenReturn(true);
      // mock session factory
      SessionFactory mockSessionFactory = mock(SessionFactory.class);
      when(mockSessionFactory.createSession(Matchers.anyCollectionOf(Class.class).toArray(new Class<?>[0]), Matchers.anyCollectionOf(Object.class).toArray())).thenReturn("testsession");
      when(mockEndPoint.getSessionFactory()).thenReturn(mockSessionFactory);
      
      // create the SLSB nointerface view binder
      SessionAwareNoInterfaceViewJNDIBinder jndiBinder = new SessionAwareNoInterfaceViewJNDIBinder(mockKernelControllerCtx);
      Context jndiCtx = this.getJNDIContext();
      
      // create metadata for the SLSB
      JBossMetaData metadata = this.createMetaData(GrandChildSFSB.class);
      JBossSessionBean31MetaData sfsbMetadata = (JBossSessionBean31MetaData) metadata.getEnterpriseBean(GrandChildSFSB.class
            .getSimpleName());
      
      // bind
      jndiBinder.bindNoInterfaceView(jndiCtx, GrandChildSFSB.class, sfsbMetadata);

      // now lookup the bound no-interface view and carry out the type compatible tests
      String jndiName = GrandChildSFSB.class.getSimpleName() + "/no-interface";
      Object noInterfaceView = jndiCtx.lookup(jndiName);

      Assert.assertNotNull("Lookup of nointerface view returned null", noInterfaceView);

      Assert.assertTrue("no-interface view is not of type: " + GrandChildSFSB.class, noInterfaceView instanceof GrandChildSFSB);

      Class<?> superClass = GrandChildSFSB.class.getSuperclass();
      while (superClass != null)
      {
         Assert.assertTrue("no-interface view is not type-compatible, can't be cast to: " + superClass, superClass
               .isInstance(noInterfaceView));
         Class<?> interfaces[] = superClass.getInterfaces();
         for (Class<?> interfaceType : interfaces)
         {
            Assert.assertTrue("no-interface view is not type-compatible, can't be cast to: " + interfaceType,
                  interfaceType.isInstance(noInterfaceView));
         }
         superClass = superClass.getSuperclass();
      }

   }

   /**
    * Returns the JNDI {@link Context} to be used in the tests
    * @return
    * @throws NamingException
    */
   private Context getJNDIContext() throws NamingException
   {
      Properties props = new Properties();
      props.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.LocalOnlyContextFactory");
      props.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
      return new InitialContext(props);

   }
   
   private JBossMetaData createMetaData(Class<?>... classes)
   {
      AnnotationFinder<AnnotatedElement> finder = new DefaultAnnotationFinder<AnnotatedElement>();
      JBoss50Creator creator = new JBoss50Creator(finder);
      JBossMetaData metadata = creator.create(Arrays.asList(classes));
      return metadata;
   }
}
