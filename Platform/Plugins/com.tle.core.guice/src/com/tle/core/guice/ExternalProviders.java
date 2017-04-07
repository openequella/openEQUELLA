package com.tle.core.guice;

import java.util.List;
import java.util.Set;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;
import com.tle.core.guice.GuicePlugin.GuiceBeanLocator;
import com.tle.core.plugins.PluginBeanLocator;

public class ExternalProviders extends AbstractModule
{
	private final Iterable<Module> modules;
	private final List<PluginBeanLocator> locators;

	public ExternalProviders(List<PluginBeanLocator> locators, Iterable<Module> modules)
	{
		this.modules = modules;
		this.locators = locators;
	}

	@Override
	protected void configure()
	{
		binder().requireExplicitBindings();
		List<Element> elements = Elements.getElements(modules);
		ElementAnalyzer analyzer = new ElementAnalyzer(binder());
		for( Element element : elements )
		{
			element.acceptVisitor(analyzer);
		}
		analyzer.throwErrorIfNeeded();
		Set<Key<?>> external = analyzer.getExternalDependencies();
		for( Key<?> key : external )
		{
			bindExternal(key);
		}
	}

	private <T> void bindExternal(Key<T> key)
	{
		bind(key).toProvider(new ExternalProvider<T>(key));
	}

	public class ExternalProvider<T> implements Provider<T>
	{

		private Key<T> key;

		public ExternalProvider(Key<T> key)
		{
			this.key = key;
		}

		@SuppressWarnings("unchecked")
		@Override
		public T get()
		{
			for( PluginBeanLocator locator : locators )
			{
				T obj;
				if( locator instanceof GuiceBeanLocator )
				{
					obj = ((GuiceBeanLocator) locator).getBeanForKey(key);
				}
				else
				{
					obj = (T) locator.getBeanForType(key.getTypeLiteral().getRawType());
				}
				if( obj != null )
				{
					return obj;
				}
			}
			throw new RuntimeException("Couldn't find bean for type:" + key); //$NON-NLS-1$
		}
	}

}
