/**
 * 
 */
package org.jboss.ejb3.nointerface.test.viewcreator;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import javax.ejb.Stateful;

/**
 * StatefulBeanWithInterfaces
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
@Stateful
public class StatefulBeanWithInterfaces implements MyStateful, Serializable, Externalizable
{

   private int counter = 1;

   public int getNextCounter()
   {
      return ++counter;
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
