package com.tle.core.initialiser;

import net.sf.beanlib.PropertyInfo;
import net.sf.beanlib.spi.BeanTransformerSpi;
import net.sf.beanlib.spi.CustomBeanTransformerSpi;
import net.sf.beanlib.spi.CustomBeanTransformerSpi.Factory;

import org.hibernate.proxy.HibernateProxyHelper;

import com.google.common.base.Throwables;
import com.tle.beans.entity.BaseEntity;

public class BaseEntityTransformer implements Factory, CustomBeanTransformerSpi
{
	@Override
	public CustomBeanTransformerSpi newCustomBeanTransformer(BeanTransformerSpi contextBeanTransformer)
	{
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T transform(Object in, Class<T> toClass, PropertyInfo propertyInfo)
	{
		try
		{
			Class<?> newClass = HibernateProxyHelper.getClassWithoutInitializingProxy(in);
			BaseEntity node = (BaseEntity) newClass.newInstance();
			BaseEntity inObj = (BaseEntity) in;
			node.setUuid(inObj.getUuid());
			node.setId(inObj.getId());
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
		return BaseEntity.class.isAssignableFrom(toClass);
	}
}