/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.nointerface.impl.test;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.jboss.ejb3.async.spi.AsyncEndpoint;
import org.jboss.ejb3.async.spi.AsyncInvocationId;
import org.jboss.ejb3.endpoint.Endpoint;
import org.jboss.ejb3.endpoint.SessionFactory;

/**
 * Mock endpoint providing NOOP for all contracts
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 */
public class MockEndpoint implements AsyncEndpoint, Endpoint
{

   @Override
   public boolean cancel(AsyncInvocationId id) throws IllegalArgumentException
   {
      // TODO Auto-generated method stub
      return false;
   }

   @Override
   public Object invokeAsync(Serializable session, Class<?> invokedBusinessInterface, Method method, Object[] args)
         throws Throwable
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public SessionFactory getSessionFactory() throws IllegalStateException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Object invoke(Serializable session, Class<?> invokedBusinessInterface, Method method, Object[] args)
         throws Throwable
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public boolean isSessionAware()
   {
      // TODO Auto-generated method stub
      return false;
   }

}