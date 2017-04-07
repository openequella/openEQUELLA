package com.tle.core.plugins;

import java.io.Serializable;

public interface BeanLocator<T> extends Serializable
{
	T get();
}
