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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

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
import org.jboss.ejb3.nointerface.impl.jndi.AbstractNoInterfaceViewBinder;
import org.jboss.ejb3.nointerface.impl.jndi.SessionlessBeanNoInterfaceViewBinder;
import org.jboss.ejb3.nointerface.impl.jndi.StatefulBeanNoInterfaceViewBinder;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeansMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBean31MetaData;
import org.jboss.metadata.ejb.jboss.jndi.resolver.impl.JNDIPolicyBasedSessionBean31JNDINameResolver;

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
         this.createAndAttachNoInterfaceViewBinder(unit, beanClass, sessionBeanMetaData);

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
   
   private void createAndAttachNoInterfaceViewBinder(DeploymentUnit unit, Class<?> beanClass, JBossSessionBean31MetaData sessionBean)
   {
      Context initCtx;
      try
      {
         initCtx = new InitialContext();
      }
      catch (NamingException ne)
      {
         throw new RuntimeException(ne);
      }
      JNDIPolicyBasedSessionBean31JNDINameResolver jndiNameResolver = new JNDIPolicyBasedSessionBean31JNDINameResolver();
      String jndiName = jndiNameResolver.resolveNoInterfaceJNDIName(sessionBean);
      AbstractNoInterfaceViewBinder binder = null;
      if (sessionBean.isStateful())
      {
         binder = new StatefulBeanNoInterfaceViewBinder(initCtx, jndiName, beanClass, sessionBean);
      }
      else
      {
         binder = new SessionlessBeanNoInterfaceViewBinder(initCtx, jndiName, beanClass, sessionBean);
      }
      String containerName = sessionBean.getContainerName();
      String binderName = containerName + ",type=nointerface-view-jndi-binder";
      
      BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder(binderName, binder.getClass().getName());
      builder.setConstructorValue(binder);
      
      // add jndi: supply
      builder.addSupply("jndi:" + jndiName);

      // add dependency
      AbstractInjectionValueMetaData injectMetaData = new AbstractInjectionValueMetaData(containerName);
      // EJBTHREE-2166 - Depending on DESCRIBED state and then pushing to INSTALLED
      // through MC API, won't work. So for now, just depend on INSTALLED state.
      //injectMetaData.setDependentState(ControllerState.DESCRIBED);
      injectMetaData.setDependentState(ControllerState.INSTALLED);
      injectMetaData.setFromContext(FromContext.CONTEXT);

      // Too bad we have to know the field name. Need to do more research on MC to see if we can
      // add property metadata based on type instead of field name.
      builder.addPropertyMetaData("endpointContext", injectMetaData);
      
      if (unit.isComponent())
      {
         // Attach it to parent since we are processing a component DU and BeanMetaDataDeployer doesn't
         // pick up BeanMetaData from component DU
         unit.getParent().addAttachment(BeanMetaData.class + ":" + binderName, builder.getBeanMetaData());
      }
      else
      {
         unit.addAttachment(BeanMetaData.class + ":" + binderName, builder.getBeanMetaData());
      }
      
      logger.debug("No-interface JNDI binder for container " + containerName + " has been created and added to the deployment unit " + unit);

   }

}
