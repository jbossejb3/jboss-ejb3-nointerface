/*
 * JBoss, Home of Professional Open Source.
 * Copyright (c) 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.ejb3.nointerface.impl.test.ejbthree2217.unit;

import org.jboss.ejb3.nointerface.impl.invocationhandler.NoInterfaceViewInvocationHandler;
import org.jboss.ejb3.nointerface.impl.test.ejbthree2217.Magician;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.Serializable;
import java.lang.reflect.Proxy;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class HashCodeTestCase
{
   private static Magician magician;

   @BeforeClass
   public static void beforeClass()
   {
      KernelControllerContext endpointContext = mock(KernelControllerContext.class);
      when(endpointContext.getTarget()).thenThrow(new RuntimeException("Do not call getTarget()"));
      Serializable session = null;
      Class<?> businessInterface = Magician.class;

      NoInterfaceViewInvocationHandler handler = new NoInterfaceViewInvocationHandler(endpointContext, session, businessInterface);

      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      Class<?> interfaces[] = { businessInterface };
      magician = (Magician) Proxy.newProxyInstance(loader, interfaces, handler);
   }

   @Test
   public void testEquals()
   {
      boolean result = magician.equals(magician);
      assertTrue(result);
   }

   @Test
   public void testHashCode()
   {
      magician.hashCode();
      // be content with any answer
   }

   @Test
   public void testToString()
   {
      magician.toString();
      // be context with any answer
   }
}
