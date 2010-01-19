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
package org.jboss.ejb3.nointerface.test.viewcreator;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import javax.ejb.Stateful;

/**
 * SimpleSFSBeanWithoutInterfaces
 * 
 * Used in testing of no-interface view. Although the name suggests
 * this bean does not implement any interfaces, it does however implement
 * {@link Serializable} and {@link Externalizable} which are allowed by 
 * spec for no-interface view.
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
@Stateful
public class SimpleSFSBeanWithoutInterfaces implements Serializable, Externalizable
{

   public static final int INITIAL_QTY = 2;

   private int qtyPurchased = INITIAL_QTY;

   public int getQtyPurchased()
   {
      return this.qtyPurchased;
   }

   public void incrementPurchaseQty()
   {
      this.qtyPurchased++;
   }

   public void incrementPurchaseQty(int qty)
   {
      this.qtyPurchased += qty;
   }

   public static void someStaticMethod()
   {
      // do nothing
   }

   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
   {
      // do nothing

   }

   public void writeExternal(ObjectOutput out) throws IOException
   {
      // do nothing

   }
}
