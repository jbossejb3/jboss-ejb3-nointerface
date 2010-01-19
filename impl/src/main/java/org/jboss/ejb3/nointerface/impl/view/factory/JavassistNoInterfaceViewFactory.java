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
package org.jboss.ejb3.nointerface.impl.view.factory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.LoaderClassPath;
import javassist.Modifier;

import org.jboss.ejb3.nointerface.spi.view.factory.NoInterfaceViewFactory;
import org.jboss.logging.Logger;

/**
 * NoInterfaceEJBViewFactoryBase
 *
 * Creates a no-interface view for a EJB as per the EJB3.1 spec (section 3.4.4)
 *
 * @see NoInterfaceViewFactory
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class JavassistNoInterfaceViewFactory implements NoInterfaceViewFactory
{

   /**
    * The proxies (sub-classes) created for the bean class need to be
    * unique. This unique number is appended to the generated class name
    */
   private static long nextUniqueNumberForNoViewInterfaceClassName = 0;

   /**
    * Used while generating unique number for the proxy class
    */
   private static Object nextUniqueNumberLock = new Object();

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(JavassistNoInterfaceViewFactory.class);

   /**
    * Inspects the bean class for all public methods and creates a proxy (sub-class)
    * out of it with overriden implementation of the public methods. The overriden
    * implementation will just give a call to <code>container</code>'s invoke(...) method
    * which handles the actual call.
    *
    * @param <T>
    * @param invocationHandler The container correpsonding to the bean class
    * @param beanClass The bean class (currently assumed)
    * @return Returns the no-interface view for the <code>beanClass</code>
    * @throws Exception
    */
   public <T> T createView(InvocationHandler invocationHandler, Class<T> beanClass) throws Exception
   {
      if (logger.isTraceEnabled())
      {
         logger.trace("Creating nointerface view for beanClass: " + beanClass + " with container " + invocationHandler);
      }

      // Create a ClassPool and add the classpath using the classloader of the beanClass, so 
      // that it uses the correct jar while looking up the class
      ClassPool pool = new ClassPool();
      pool.appendClassPath(new LoaderClassPath(beanClass.getClassLoader()));
      
      CtClass beanCtClass = pool.get(beanClass.getName());

      // Create a sub-class (proxy) for the bean class. A unique name will be used for the subclass
      CtClass proxyCtClass = pool.makeClass(beanClass.getName() + "_NoInterfaceProxy$" + getNextUniqueNumber(),
            beanCtClass);

      // We need to maintain a reference of the invocationHandler in the proxy, so that we can
      // forward the method calls to invocationHandler.invoke. Create a new field in the sub-class (proxy)
      CtField invocationHandlerField = CtField.make("private java.lang.reflect.InvocationHandler invocationHandler;",
            proxyCtClass);
      proxyCtClass.addField(invocationHandlerField);

      // get all public methods from the bean class
      Set<CtMethod> beanPublicMethods = getAllPublicNonStaticNonFinalMethods(beanCtClass);

      // Override each of the public methods
      for (CtMethod beanPublicMethod : beanPublicMethods)
      {
         // Methods from java.lang.Object can be skipped, if they are
         // not implemented (overriden) in the bean class. TODO: Do we really need to do this?
         if (shouldMethodBeSkipped(pool, beanPublicMethod))
         {
            logger.debug("Skipping " + beanPublicMethod.getName() + " on bean " + beanCtClass.getName()
                  + " from no-interface view");
            continue;
         }
         // We should not be changing the bean class methods. So we need to create a copy of the methods
         // for the sub-class (proxy)
         CtMethod proxyPublicMethod = CtNewMethod.copy(beanPublicMethod, proxyCtClass, null);
         // All the public methods of the bean should now be overriden (through the proxy)
         // to give a call to the container.invoke
         // Ex: If the bean's public method was:
         // public String sayHi(String name) { return "Hi " + name; }
         // then it will be changed in the proxy to
         // public String sayHi(String name) { java.lang.reflect.Method currentMethod = beanClass.getName() + ".class.getMethod(theMethodName,params);
         // return container.invoke(this,currentMethod,args); }
         proxyPublicMethod = overridePublicMethod(invocationHandler, beanClass, beanPublicMethod, proxyPublicMethod);
         // We have now created the overriden method. We need to add it to the proxy
         proxyCtClass.addMethod(proxyPublicMethod);
         if (logger.isTraceEnabled())
         {
            logger.trace("Added overriden implementation for method " + proxyPublicMethod.getName()
                  + " in no-interface view " + proxyCtClass.getName() + " for bean " + beanClass.getName());
         }
      }
      // Add java.io.Serializable as the interface for the proxy (since it goes into JNDI)
      //proxyCtClass.addInterface(pool.get(Serializable.class.getName()));
      proxyCtClass.addMethod(createEqualsMethod(pool, proxyCtClass));

      // We are almost done (except for setting the container field in the proxy)
      // Let's first create a java.lang.Class (i.e. load) out of the javassist class
      // using the classloader of the bean
      Class<?> proxyClass = proxyCtClass.toClass(beanClass.getClassLoader(), beanClass.getProtectionDomain());
      // time to set the container field through normal java reflection
      Object proxyInstance = proxyClass.newInstance();
      Field containerInProxy = proxyClass.getDeclaredField("invocationHandler");
      containerInProxy.setAccessible(true);
      containerInProxy.set(proxyInstance, invocationHandler);

      // return the proxy instance
      return beanClass.cast(proxyInstance);

   }

   private <T> CtMethod overridePublicMethod(InvocationHandler container, Class<T> beanClass,
         CtMethod publicMethodOnBean, CtMethod publicMethodOnProxy) throws Exception
   {
      publicMethodOnProxy.setBody("{"
            +
            // The proxy needs to call the container.invoke
            // the InvocationHandler.invoke accepts (Object proxy,Method method,Object[] args)
            // This view needs to create a java.lang.reflect.Method object based on the "current method"
            // that is invoked on the view. Note that we need to get the Method from the beanclass.
            // Note: All the '$' parameters used are javassist specific syntax
            "java.lang.reflect.Method currentMethod = " + beanClass.getName() + ".class.getMethod(\""
            + publicMethodOnBean.getName() + "\",$sig);" +
            // At this point we have the container, the Method to be invoked and the parameters to be passed
            // All we have to do is invoke the container
            "return ($r) invocationHandler.invoke(this,currentMethod,$args);" + "}");

      return publicMethodOnProxy;
   }

   /**
    * Returns all public, non-static and non-final methods for the class
    *
    * @param ctClass The class whose non-final, non-static public methods are to be returned
    * @return
    * @throws Exception
    */
   private Set<CtMethod> getAllPublicNonStaticNonFinalMethods(CtClass ctClass) throws Exception
   {
      CtMethod[] allMethods = ctClass.getMethods();
      Set<CtMethod> publicMethods = new HashSet<CtMethod>();

      for (CtMethod ctMethod : allMethods)
      {
         int modifier = ctMethod.getModifiers();
         // Public non-static non-final methods
         if (((Modifier.PUBLIC & modifier) == Modifier.PUBLIC) && ((Modifier.STATIC & modifier) != Modifier.STATIC)
               && ((Modifier.FINAL & modifier) != Modifier.FINAL) && ((Modifier.NATIVE & modifier) != Modifier.NATIVE))
         {
            publicMethods.add(ctMethod);
         }
      }
      return publicMethods;
   }

   /**
    * Checks whether a method has to be skipped from being overriden in the proxy
    * that is returned for the no-interface view.
    *
    * TODO: Do we really need this. Need to think more. Let's keep it for the time-being
    *
    * @param beanCtClass
    * @param ctMethod
    * @return
    * @throws Exception
    */
   private boolean shouldMethodBeSkipped(CtClass beanCtClass, CtMethod ctMethod) throws Exception
   {

      //      List<CtMethod> declaredMethods = Arrays.asList(beanCtClass.getDeclaredMethods());
      //      if (declaredMethods.contains(ctMethod))
      //      {
      //         return false;
      //      }
      //      CtClass objectCtClass = ClassPool.getDefault().get(Object.class.getName());
      //      CtMethod[] methodsInObjectClass = objectCtClass.getMethods();
      //      List<CtMethod> methodsToBeSkipped = Arrays.asList(methodsInObjectClass);
      //      return methodsToBeSkipped.contains(ctMethod);
      return false;

   }

   private static boolean shouldMethodBeSkipped(ClassPool pool, CtMethod ctMethod) throws Exception
   {
      CtClass paramsToEqualsMethodInObjectClass[] = new CtClass[]
      {pool.get(Object.class.getName())};
      if (!ctMethod.getName().equals("equals"))
      {
         return false;
      }
      if (ctMethod.getParameterTypes().length != paramsToEqualsMethodInObjectClass.length)
      {
         return false;
      }
      CtClass paramsToEqualsMethodInOtherClass[] = ctMethod.getParameterTypes();
      return paramsToEqualsMethodInObjectClass[0].equals(paramsToEqualsMethodInOtherClass[0]);

   }

   private static CtMethod createEqualsMethod(ClassPool pool, CtClass proxyCtClass) throws Exception
   {
      String body = "{" + "java.lang.reflect.Method currentMethod = " + Object.class.getName()
            + ".class.getMethod(\"equals\",$sig);" + "return ($r) invocationHandler.invoke(this,currentMethod,$args);"
            + "}";

      Method equals = Object.class.getMethod("equals", new Class<?>[]
      {Object.class});
      CtClass returnType = pool.get(equals.getReturnType().getName());
      CtClass paramTypes[] = new CtClass[equals.getParameterTypes().length];
      int i = 0;
      for (Class<?> paramType : equals.getParameterTypes())
      {
         paramTypes[i++] = pool.get(paramType.getName());
      }

      CtClass exceptionTypes[] = new CtClass[equals.getExceptionTypes().length];
      int j = 0;
      for (Class<?> exceptionType : equals.getExceptionTypes())
      {
         exceptionTypes[j++] = pool.get(exceptionType.getName());
      }

      return CtNewMethod.make(returnType, equals.getName(), paramTypes, exceptionTypes, body, proxyCtClass);
   }

   /**
    * Get the next unique number which will be used for the proxy class name
    *
    * @return
    */
   private long getNextUniqueNumber()
   {
      synchronized (nextUniqueNumberLock)
      {
         this.nextUniqueNumberForNoViewInterfaceClassName++;
         return this.nextUniqueNumberForNoViewInterfaceClassName;
      }
   }

}
