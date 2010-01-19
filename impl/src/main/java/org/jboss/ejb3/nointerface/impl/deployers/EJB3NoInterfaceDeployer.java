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
package org.jboss.ejb3.nointerface.impl.deployers;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.InitialContext;

import org.jboss.beans.metadata.api.model.FromContext;
import org.jboss.beans.metadata.plugins.AbstractInjectionValueMetaData;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.ejb3.deployers.Ejb3MetadataProcessingDeployer;
import org.jboss.ejb3.nointerface.impl.jndi.NoInterfaceViewJNDIBinderFacade;
import org.jboss.logging.Logger;
import org.jboss.metadata.ear.jboss.JBossAppMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeansMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBean31MetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;

/**
 * EJB3NoInterfaceDeployer
 * 
 * Deployer responsible for processing EJB3 deployments with a no-interface view.
 * @see #deploy(DeploymentUnit) for the deployment unit processing details.
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class EJB3NoInterfaceDeployer extends AbstractDeployer
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(EJB3NoInterfaceDeployer.class);

   /**
    * We need processed metadata
    */
   private static final String INPUT = Ejb3MetadataProcessingDeployer.OUTPUT;

   /**
    * Constructor
    */
   public EJB3NoInterfaceDeployer()
   {
      setStage(DeploymentStages.REAL);
      setInput(JBossMetaData.class);
      addInput(INPUT);
      // we deploy MC beans
      addOutput(BeanMetaData.class);

   }

   /**
    * Process the deployment unit and deploy appropriate MC beans (see details below)
    * if it corresponds to a no-interface view deployment.
    * 
    * If any beans in the unit are eligible for no-interface view, then internally this method
    * creates a {@link NoInterfaceViewJNDIBinderFacade} MC bean for the no-interface view.
    * 
    * The {@link NoInterfaceViewJNDIBinderFacade}, thus created, will be dependent on the {@link ControllerState#DESCRIBED}
    * state of the container (endpoint) MC bean. This way, we ensure that this {@link NoInterfaceViewJNDIBinderFacade}
    * will be deployed only after the corresponding container MC bean moves to {@link ControllerState#DESCRIBED}
    * state.
    */
   public void deploy(DeploymentUnit unit) throws DeploymentException
   {

      if (logger.isTraceEnabled())
      {
         logger.trace("Deploying unit " + unit.getName());
      }
      // get processed metadata
      JBossMetaData metaData = unit.getAttachment(INPUT, JBossMetaData.class);
      if (metaData == null)
      {
         if (logger.isTraceEnabled())
            logger.trace("No JBossMetadata for unit : " + unit.getName());
         return;
      }
      // work on the ejbs
      JBossEnterpriseBeansMetaData beans = metaData.getEnterpriseBeans();
      for (JBossEnterpriseBeanMetaData bean : beans)
      {
         if (bean.isSession())
         {
            if (logger.isTraceEnabled())
            {
               logger.trace("Found bean of type session: " + bean.getEjbClass() + " in unit " + unit.getName());
            }
            // too bad
            if (bean instanceof JBossSessionBean31MetaData)
            {
               // Process for no-interface view
               deploy(unit, (JBossSessionBean31MetaData) bean);
            }
         }
      }

   }

   /**
    * Creates a {@link NoInterfaceViewJNDIBinderFacade} MC bean for the no-interface view represented by the
    * <code>sessionBeanMetaData</code>. The {@link NoInterfaceViewJNDIBinderFacade} is created only
    * if the bean is eligible for a no-interface view as defined by the EJB3.1 spec
    * 
    * The {@link NoInterfaceViewJNDIBinderFacade}, thus created, will be dependent on the {@link ControllerState#DESCRIBED}
    * state of the container (endpoint) MC bean. This way, we ensure that this {@link NoInterfaceViewJNDIBinderFacade}
    * will be deployed only after the corresponding container MC bean moves to {@link ControllerState#DESCRIBED}
    * state.
    *
    * @param unit Deployment unit
    * @param sessionBeanMetaData Session bean metadata
    * @throws DeploymentException If any exceptions are encountered during processing of the deployment unit
    */
   private void deploy(DeploymentUnit unit, JBossSessionBean31MetaData sessionBeanMetaData) throws DeploymentException
   {
      try
      {
         if (!sessionBeanMetaData.isNoInterfaceBean())
         {
            if (logger.isTraceEnabled())
            {
               logger.trace("Bean class " + sessionBeanMetaData.getEjbClass()
                     + " is not eligible for no-interface view");
            }
            return;
         }
         Class<?> beanClass = Class.forName(sessionBeanMetaData.getEjbClass(), false, unit.getClassLoader());

         String containerMCBeanName = sessionBeanMetaData.getContainerName();
         if (logger.isTraceEnabled())
         {
            logger.trace("Container name for bean " + sessionBeanMetaData.getEjbName() + " in unit " + unit + " is "
                  + containerMCBeanName);
         }
         if (containerMCBeanName == null)
         {
            // The container name is set in the metadata only after the creation of the container
            // However, this deployer does not have an dependency on the creation of a container,
            // so getting the container name from the bean metadata won't work. Need to do a different/better way
            //String containerMCBeanName = sessionBeanMetaData.getContainerName();
            containerMCBeanName = getContainerName(unit, sessionBeanMetaData);

         }

         // Create the NoInterfaceViewJNDIBinder (MC bean) and add a dependency on the DESCRIBED
         // state of the container (endpoint) MC bean
         NoInterfaceViewJNDIBinderFacade noInterfaceViewJNDIBinderFacade = new NoInterfaceViewJNDIBinderFacade(
               new InitialContext(), beanClass, sessionBeanMetaData);
         String noInterfaceViewMCBeanName = unit.getName() + "$" + sessionBeanMetaData.getEjbName();
         BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder(noInterfaceViewMCBeanName,
               noInterfaceViewJNDIBinderFacade.getClass().getName());
         builder.setConstructorValue(noInterfaceViewJNDIBinderFacade);

         // add dependency
         AbstractInjectionValueMetaData injectMetaData = new AbstractInjectionValueMetaData(containerMCBeanName);
         injectMetaData.setDependentState(ControllerState.DESCRIBED);
         injectMetaData.setFromContext(FromContext.CONTEXT);

         // Too bad we have to know the field name. Need to do more research on MC to see if we can
         // add property metadata based on type instead of field name.
         builder.addPropertyMetaData("endpointContext", injectMetaData);

         // Add this as an attachment
         unit.addAttachment(BeanMetaData.class + ":" + noInterfaceViewMCBeanName, builder.getBeanMetaData());

         logger.debug("No-interface JNDI binder for container " + containerMCBeanName
               + " has been created and added to the deployment unit " + unit);

      }
      catch (Throwable t)
      {
         DeploymentException.rethrowAsDeploymentException("Could not create no-interface view for "
               + sessionBeanMetaData.getEjbClass() + " in unit " + unit.getName(), t);
      }
   }

   /**
    * Undeploy
    * 
    * @param unit
    * @param deployment
    */
   public void undeploy(DeploymentUnit unit, JBossMetaData deployment)
   {
      // TODO Needs implementation

   }
   
   /**
    * Ultimately, the container name should come from the <code>sessionBeanMetadata</code>.
    * However because of the current behaviour where the container on its start sets the containername
    * in the metadata, its not possible to get this information even before the container is started.
    *
    * Hence let's for the time being create the container name from all the information that we have
    * in the <code>unit</code>
    *
    * @param unit The deployment unit
    * @param sessionBeanMetadata Session bean metadata
    * @return Returns the container name for the bean corresponding to the <code>sessionBeanMetadata</code> in the <code>unit</code>
    *
    * @throws MalformedObjectNameException
    */
   private String getContainerName(DeploymentUnit unit, JBossSessionBeanMetaData sessionBeanMetadata)
         throws MalformedObjectNameException
   {
      // TODO the base ejb3 jmx object name comes from Ejb3Module.BASE_EJB3_JMX_NAME, but
      // we don't need any reference to ejb3-core. Right now just hard code here, we need
      // a better way/place for this later
      StringBuilder containerName = new StringBuilder("jboss.j2ee:service=EJB3" + ",");

      // Get the top level unit for this unit (ex: the top level might be an ear and this unit might be the jar
      // in that ear
      DeploymentUnit toplevelUnit = unit.getTopLevel();
      if (toplevelUnit != null)
      {
         // if top level is an ear, then create the name with the ear reference
         if (isEar(toplevelUnit))
         {
            containerName.append("ear=");
            containerName.append(toplevelUnit.getSimpleName());
            containerName.append(",");

         }
      }
      // now work on the passed unit, to get the jar name
      if (unit.getSimpleName() == null)
      {
         containerName.append("*");
      }
      else
      {
         containerName.append("jar=");
         containerName.append(unit.getSimpleName());
      }
      // now the ejbname
      containerName.append(",name=");
      containerName.append(sessionBeanMetadata.getEjbName());

      if (logger.isTraceEnabled())
      {
         logger.trace("Container name generated for ejb = " + sessionBeanMetadata.getEjbName() + " in unit " + unit
               + " is " + containerName);
      }
      ObjectName containerJMXName = new ObjectName(containerName.toString());
      return containerJMXName.getCanonicalName();
   }

   /**
    * Returns true if this <code>unit</code> represents an .ear deployment
    *
    * @param unit
    * @return
    */
   private boolean isEar(DeploymentUnit unit)
   {
      return unit.getSimpleName().endsWith(".ear") || unit.getAttachment(JBossAppMetaData.class) != null;
   }
}
