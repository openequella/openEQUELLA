package com.tle.core.plugins;

import java.io.Serializable;

public class SerializedBeanLocator<T extends Serializable> implements BeanLocator<T>
{
	private final T object;

	public SerializedBeanLocator(T object)
	{
		this.object = object;
	}

	@Override
	public T get()
	{
		return object;
	}

}
