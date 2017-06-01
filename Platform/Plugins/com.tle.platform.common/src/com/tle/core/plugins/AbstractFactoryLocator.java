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

package com.tle.core.plugins;

public abstract class AbstractFactoryLocator<F, T> implements BeanLocator<T>
{
	private static final long serialVersionUID = 1L;
	private final Class<F> factoryClass;

	public AbstractFactoryLocator(Class<F> factoryClass)
	{
		this.factoryClass = factoryClass;
	}

	@SuppressWarnings("nls")
	@Override
	public T get()
	{
		PluginService pluginService = AbstractPluginService.get();
		String pluginId = pluginService.getPluginIdForObject(factoryClass);
		F factory = factoryClass.cast(AbstractPluginService.get().getBean(pluginId, "bean:" + factoryClass.getName()));
		return invoke(factory);
	}

	protected abstract T invoke(F factory);

}
