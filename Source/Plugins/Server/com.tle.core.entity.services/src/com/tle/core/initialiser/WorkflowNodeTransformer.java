package com.tle.core.initialiser;

import net.sf.beanlib.PropertyInfo;
import net.sf.beanlib.spi.BeanTransformerSpi;
import net.sf.beanlib.spi.CustomBeanTransformerSpi;
import net.sf.beanlib.spi.CustomBeanTransformerSpi.Factory;

import org.hibernate.proxy.HibernateProxyHelper;

import com.google.common.base.Throwables;
import com.tle.common.workflow.Workflow;
import com.tle.common.workflow.node.WorkflowNode;

public class WorkflowNodeTransformer implements Factory, CustomBeanTransformerSpi
{

	@SuppressWarnings("unchecked")
	@Override
	public <T> T transform(Object in, Class<T> toClass, PropertyInfo propertyInfo)
	{
		try
		{
			Class<?> newClass = HibernateProxyHelper.getClassWithoutInitializingProxy(in);
			WorkflowNode node = (WorkflowNode) newClass.newInstance();
			WorkflowNode inNode = (WorkflowNode) in;
			node.setUuid(inNode.getUuid());
			node.setId(inNode.getId());
			Workflow workflow = new Workflow();
			workflow.setUuid(inNode.getWorkflow().getUuid());
			node.setWorkflow(workflow);
			node.setChildren(inNode.getChildren());
			return (T) node;
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
	}

	@Override
	public boolean isTransformable(Object from, Class<?> toClass, PropertyInfo propertyInfo)
	{
		return WorkflowNode.class.isAssignableFrom(toClass);
	}

	@Override
	public CustomBeanTransformerSpi newCustomBeanTransformer(BeanTransformerSpi contextBeanTransformer)
	{
		return this;
	}

}
