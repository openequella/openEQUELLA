package com.tle.core.plugins;

import java.io.Serializable;

import org.apache.commons.beanutils.MethodUtils;

public class FactoryMethodLocator<T> implements BeanLocator<T>
{
	private static final long serialVersionUID = 1L;

	private final String methodName;
	private final Serializable[] args;
	private final Class<?> clazz;

	public FactoryMethodLocator(Class<?> clazz, String methodName, Serializable... args)
	{
		this.clazz = clazz;
		this.methodName = methodName;
		this.args = args;
	}

	protected Object[] getArgs()
	{
		return args;
	}

	@SuppressWarnings({"unchecked", "nls"})
	protected <F> F getFactory()
	{
		PluginService pluginService = AbstractPluginService.get();
		String pluginId = pluginService.getPluginIdForObject(clazz);
		return (F) AbstractPluginService.get().getBean(pluginId, "bean:" + clazz.getName());
	}

	@Override
	public T get()
	{
		Object factory = getFactory();
		return invokeFactoryMethod(factory);
	}

	@SuppressWarnings("unchecked")
	protected T invokeFactoryMethod(Object factory)
	{
		try
		{
			return (T) MethodUtils.invokeMethod(factory, getMethodName(), getArgs());
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	protected String getMethodName()
	{
		return methodName;
	}

}
