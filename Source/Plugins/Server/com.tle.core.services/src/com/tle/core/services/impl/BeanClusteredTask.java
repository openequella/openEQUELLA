/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.services.impl;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.apache.commons.beanutils.MethodUtils;

import com.tle.core.plugins.PluginService;

public class BeanClusteredTask implements ClusteredTask
{
	private static final long serialVersionUID = 1L;

	private final Class<?> clazz;
	private final String globalId;
	private final String beanName;
	private final String methodName;
	private final boolean isTransient;
	private final boolean isPriority;
	private final transient Serializable[] args;

	public BeanClusteredTask(String globalId, Class<?> service, String methodName, Serializable... args)
	{
		this(globalId, service, service.getName(), methodName, false, false, args);
	}

	public BeanClusteredTask(String globalId, boolean priority, Class<?> service, String methodName,
		Serializable... args)
	{
		this(globalId, service, service.getName(), methodName, false, priority, args);
	}

	/**
	 * Low priority tasks, not expected to be restored on re-start
	 * 
	 * @param isTransient
	 * @param globalId
	 * @param service
	 * @param methodName
	 * @param args
	 */
	public BeanClusteredTask(boolean isTransient, String globalId, Class<?> service, String methodName,
		Serializable... args)
	{
		this(globalId, service, service.getName(), methodName, isTransient, false, args);
	}

	private BeanClusteredTask(String globalId, Class<?> service, String beanName, String methodName,
		boolean isTransient, boolean isPriority, Serializable... args)
	{
		this.isTransient = isTransient;
		this.globalId = globalId;
		this.clazz = service;
		this.beanName = beanName;
		this.args = args;
		this.methodName = methodName;
		this.isPriority = isPriority;
	}

	@Override
	public String getInternalId()
	{
		return globalId != null ? globalId : beanName;
	}

	@Override
	public Serializable[] getArgs()
	{
		return args;
	}

	@SuppressWarnings("nls")
	@Override
	public Task createTask(PluginService pluginService, final Serializable[] args)
	{
		String pluginId = pluginService.getPluginIdForObject(clazz);
		final Object origBean = pluginService.getBean(pluginId, "bean:" + beanName); //$NON-NLS-1$
		Class<?>[] argTypes = new Class<?>[args.length];
		for( int i = 0; i < args.length; i++ )
		{
			argTypes[i] = args[i].getClass();
		}
		final Method method = MethodUtils.getMatchingAccessibleMethod(origBean.getClass(), methodName, argTypes);
		if( method == null )
		{
			// Construct a string of the argument types, for the lucidity of
			// error message
			String argTypesStr = null;
			if( argTypes.length > 0 )
			{
				StringBuffer sb = new StringBuffer();
				for( int i = 0; i < argTypes.length; ++i )
				{
					sb.append(argTypes[i].getSimpleName());
					if( i < argTypes.length + 1 )
					{
						sb.append(", ");
					}
				}
				argTypesStr = sb.toString();
			}
			else
			{
				argTypesStr = "(no arguments)";
			}
			throw new Error("No method named:" + methodName + " with arg types:" + argTypesStr + " on "
				+ origBean.getClass());
		}

		if( Task.class.isAssignableFrom(method.getReturnType()) )
		{
			try
			{
				return (Task) method.invoke(origBean, (Object[]) args);
			}
			catch( Exception e )
			{
				throw new RuntimeException(e);
			}
		}

		return new SingleShotTask()
		{
			@Override
			public void runTask() throws Exception
			{
				try
				{
					method.invoke(origBean, (Object[]) args);
				}
				catch( Exception e )
				{
					throw new RuntimeException(e);
				}
			}

			@Override
			protected String getTitleKey()
			{
				return beanName + "." + methodName;
			}
		};
	}

	@Override
	public boolean isTransient()
	{
		return isTransient;
	}

	@Override
	public boolean isGlobal()
	{
		return globalId != null;
	}

	@Override
	public String getGlobalId()
	{
		return globalId;
	}

	@Override
	public boolean isPriority()
	{
		return isPriority;
	}

	@SuppressWarnings("nls")
	@Override
	public String toString()
	{
		return "{beanName:" + beanName + ", methodName:" + methodName + ", globalId:" + globalId + "}";
	}
}
