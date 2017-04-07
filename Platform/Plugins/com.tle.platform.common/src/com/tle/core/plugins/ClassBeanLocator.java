package com.tle.core.plugins;

public class ClassBeanLocator<T> implements BeanLocator<T>
{
	private static final long serialVersionUID = 1L;
	private final Class<? extends T> clazz;

	public ClassBeanLocator(Class<? extends T> clazz)
	{
		this.clazz = clazz;
	}

	@SuppressWarnings({"unchecked", "nls"})
	@Override
	public T get()
	{
		PluginService pluginService = AbstractPluginService.get();
		String pluginId = pluginService.getPluginIdForObject(clazz);
		return (T) AbstractPluginService.get().getBean(pluginId, "bean:" + clazz.getName());
	}
}
