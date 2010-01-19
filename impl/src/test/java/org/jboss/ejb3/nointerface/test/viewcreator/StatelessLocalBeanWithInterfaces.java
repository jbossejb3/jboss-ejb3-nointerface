/**
 * 
 */
package org.jboss.ejb3.nointerface.test.viewcreator;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;

/**
 * StatelessLocalBeanWithInterfaces
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
@Stateless
@LocalBean
public class StatelessLocalBeanWithInterfaces implements MyStateless
{

   public String sayHello(String name)
   {
      return "Hello " + name;
   }

}
