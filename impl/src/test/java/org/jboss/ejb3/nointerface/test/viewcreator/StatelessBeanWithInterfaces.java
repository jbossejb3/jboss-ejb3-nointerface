/**
 * 
 */
package org.jboss.ejb3.nointerface.test.viewcreator;

import javax.ejb.Stateless;

/**
 * StatelessBeanWithInterfaces
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
@Stateless
public class StatelessBeanWithInterfaces implements MyStateless
{

   public String sayHello(String name)
   {
      return "Hello " + name;
   }

}
