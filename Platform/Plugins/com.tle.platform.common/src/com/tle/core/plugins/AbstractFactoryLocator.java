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
