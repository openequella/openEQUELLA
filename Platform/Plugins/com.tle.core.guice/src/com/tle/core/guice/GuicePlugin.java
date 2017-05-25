package com.tle.core.guice;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.java.plugin.Plugin;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.registry.PluginPrerequisite;
import org.java.plugin.registry.PluginRegistry;
import org.java.plugin.registry.PluginRegistry.RegistryChangeData;
import org.java.plugin.registry.PluginRegistry.RegistryChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.inject.Binding;
import com.google.inject.BindingAnnotation;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.internal.Annotations;
import com.google.inject.name.Names;
import com.google.inject.spi.ConstructorBinding;
import com.google.inject.spi.LinkedKeyBinding;
import com.google.inject.spi.ProviderBinding;
import com.google.inject.spi.UntargettedBinding;
import com.tle.core.plugins.AbstractBeanLocatorCallable;
import com.tle.core.plugins.AbstractPluginService;
import com.tle.core.plugins.PluginBeanLocator;
import com.tle.core.plugins.PrivatePluginBeanLocator;
import com.tle.core.plugins.PrivatePluginService;

@SuppressWarnings("nls")
public class GuicePlugin extends Plugin implements RegistryChangeListener
{
	private PrivatePluginService privatePluginService;
	private static final Logger LOGGER = LoggerFactory.getLogger(GuicePlugin.class);
	private static final String PLUGIN_ID = "com.tle.core.guice"; //$NON-NLS-1$
	private Map<String, GuiceBeanLocator> guiceLocators = Collections.emptyMap();
	private List<BeanChecker> beanCheckers;

	@Override
	protected void doStart() throws Exception
	{
		// Please do not remove
		// This kludge is about ensuring these annotations are parsed to prevent
		// a deadlock
		Annotations.isRetainedAtRuntime(Assisted.class);
		Annotations.isRetainedAtRuntime(AssistedInject.class);
		Annotations.isRetainedAtRuntime(BindingAnnotation.class);
		// End kludge
		privatePluginService = (PrivatePluginService) AbstractPluginService.get();
		getManager().getRegistry().registerListener(this);
		setupBeanLocators();
	}

	public class GuiceBeanLocator implements PrivatePluginBeanLocator
	{
		private String pluginId;
		private Injector injector;
		private Extension extension;
		private Throwable exception;
		private AbstractBeanLocatorCallable<?> callable;
		private List<PluginBeanLocator> dependentLocators;

		public GuiceBeanLocator(String extPluginId, Extension extension)
		{
			this.pluginId = extPluginId;
			this.extension = extension;
		}

		@Override
		public String toString()
		{
			return "Guice Locator for " + pluginId; //$NON-NLS-1$
		}

		private synchronized List<PluginBeanLocator> getDependents()
		{
			if( dependentLocators == null )
			{
				Set<String> seenPlugins = new HashSet<String>();
				PluginDescriptor descriptor = extension.getDeclaringPluginDescriptor();
				ImmutableList.Builder<PluginBeanLocator> builder = ImmutableList.builder();
				for( PluginPrerequisite preRequisite : descriptor.getPrerequisites() )
				{
					String depId = preRequisite.getPluginId();
					getSecondLevelDependents(depId, seenPlugins, builder);
				}
				dependentLocators = builder.build();
			}
			return dependentLocators;
		}

		private void getSecondLevelDependents(String pluginId, Set<String> seenPlugins,
			ImmutableList.Builder<PluginBeanLocator> builder)
		{
			if( seenPlugins.contains(pluginId) )
			{
				return;
			}
			seenPlugins.add(pluginId);
			PluginBeanLocator beanLocator = privatePluginService.getBeanLocator(pluginId);
			if( beanLocator != null )
			{
				builder.add(beanLocator);
			}
			PluginDescriptor descriptor = privatePluginService.getPluginDescriptor(pluginId);
			Collection<PluginPrerequisite> prerequisites = descriptor.getPrerequisites();
			for( PluginPrerequisite pluginPrerequisite : prerequisites )
			{
				if( pluginPrerequisite.isExported() )
				{
					getSecondLevelDependents(pluginPrerequisite.getPluginId(), seenPlugins, builder);
				}
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getBean(String beanId)
		{
			Injector injector = ensureInjector();
			Key<Object> nameKey = Key.get(Object.class, Names.named(beanId));
			Binding<Object> binding = injector.getExistingBinding(nameKey);
			if( binding != null )
			{
				return (T) binding.getProvider().get();
			}
			ClassLoader classLoader = privatePluginService.getClassLoader(pluginId);
			try
			{
				Class<?> clazz = classLoader.loadClass(beanId);
				return (T) injector.getInstance(clazz);
			}
			catch( ClassNotFoundException e )
			{
				throw new RuntimeException(e);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getBeanForType(Class<T> type)
		{
			Injector injector = ensureInjector();
			Binding<?> binding = injector.getExistingBinding(Key.get(type));
			if( binding != null )
			{
				return (T) binding.getProvider().get();
			}
			return null;
		}

		@Override
		public <T> Collection<T> getBeansOfType(Class<T> clazz)
		{
			Injector injector = ensureInjector();
			List<T> beans = new ArrayList<T>();
			Map<Key<?>, Binding<?>> bindings = injector.getBindings();
			for( Binding<?> binding : bindings.values() )
			{
				Key<?> actualKey = null;
				if( binding instanceof UntargettedBinding || binding instanceof ConstructorBinding )
				{
					actualKey = binding.getKey();
				}
				else if( binding instanceof LinkedKeyBinding )
				{
					actualKey = ((LinkedKeyBinding<?>) binding).getLinkedKey();
				}
				else if( binding instanceof ProviderBinding )
				{
					actualKey = ((ProviderBinding<?>) binding).getProvidedKey();
				}
				if( actualKey != null && clazz.isAssignableFrom(actualKey.getTypeLiteral().getRawType()) )
				{
					beans.add(clazz.cast(binding.getProvider().get()));
				}
			}
			return beans;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> Class<T> loadClass(String name) throws ClassNotFoundException
		{
			PluginDescriptor descriptor = privatePluginService.getPluginDescriptor(pluginId);
			privatePluginService.ensureActivated(descriptor);
			return (Class<T>) privatePluginService.getClassLoader(descriptor).loadClass(name);
		}

		private Injector ensureInjector()
		{
			if( injector == null )
			{
				checkException();
				privatePluginService.ensureBeanLocators(Collections.singletonList(this));
				checkException();
			}
			return injector;
		}

		private void checkException()
		{
			if( exception != null )
			{
				LOGGER.error("Error creating injector for:" + pluginId, exception); //$NON-NLS-1$
				throw Throwables.propagate(exception);
			}
		}

		@SuppressWarnings("unchecked")
		public <T> T getBeanForKey(Key<T> key)
		{
			Injector injector = ensureInjector();
			Binding<T> binding = injector.getExistingBinding(key);
			if( binding == null )
			{
				binding = (Binding<T>) injector.getExistingBinding(key.ofType(key.getTypeLiteral().getRawType()));
			}
			if( binding != null )
			{
				return binding.getProvider().get();
			}
			return null;
		}

		@Override
		public <T> void autowire(T bean)
		{
			// Should never be required for guice
		}

		@Override
		public boolean isInitialised()
		{
			return injector != null;
		}

		class InjectorCreation extends AbstractBeanLocatorCallable<Injector>
		{
			public InjectorCreation()
			{
				super(GuiceBeanLocator.this);
			}

			@Override
			protected Injector doWork() throws Exception
			{
				long start = System.currentTimeMillis();
				Iterable<URL> localClassPath = privatePluginService.getLocalClassPath(pluginId);
				final ClassLoader classLoader = privatePluginService.getClassLoader(pluginId);
				Collection<Parameter> params = extension.getParameters("class");
				List<Module> modules = new ArrayList<Module>();
				for( Parameter param : params )
				{
					Module module = (Module) privatePluginService.getBean(pluginId, param.valueAsString());
					modules.add(module);
				}

				modules.add(new ScannerModule(privatePluginService, classLoader, localClassPath, getBeanCheckers()));
				injector = Guice.createInjector(new ExternalProviders(getDependents(), modules));
				long end = System.currentTimeMillis();
				LOGGER.info("Guice module for " + pluginId + " took:" + (end - start));
				return injector;
			}

			@Override
			public String toString()
			{
				return "Guice module " + pluginId; //$NON-NLS-1$
			}
		}

		@Override
		public synchronized void clearCallable()
		{
			callable = null;
		}

		@Override
		public void setThrowable(Throwable e)
		{
			this.exception = e;
		}

		@Override
		public synchronized void addCallables(List<AbstractBeanLocatorCallable<?>> callableList,
			Set<PrivatePluginBeanLocator> seenLocators)
		{
			if( exception != null )
			{
				throw Throwables.propagate(exception);
			}
			if( injector != null )
			{
				return;
			}
			if( seenLocators.contains(this) )
			{
				return;
			}
			seenLocators.add(this);
			if( callable != null )
			{
				callableList.add(callable);
				return;
			}
			boolean haveAllDependents = true;
			List<PluginBeanLocator> dependents = getDependents();
			for( PluginBeanLocator pluginBeanLocator : dependents )
			{
				if( !pluginBeanLocator.isInitialised() )
				{
					haveAllDependents = false;
					break;
				}
			}
			if( haveAllDependents )
			{
				callable = new InjectorCreation();
				callableList.add(callable);
			}
			else
			{
				dependents = getDependents();
				for( PluginBeanLocator pluginBeanLocator : dependents )
				{
					((PrivatePluginBeanLocator) pluginBeanLocator).addCallables(callableList, seenLocators);
				}
			}
		}

		@Override
		public boolean isErrored()
		{
			return exception != null;
		}

	}

	@Override
	protected void doStop() throws Exception
	{
		// nothing

	}

	protected synchronized List<BeanChecker> getBeanCheckers()
	{
		if( this.beanCheckers == null )
		{
			Collection<Extension> checkers = privatePluginService.getConnectedExtensions(PLUGIN_ID, "beanChecker");
			ImmutableList.Builder<BeanChecker> builder = ImmutableList.builder();
			for( Extension extension : checkers )
			{
				BeanChecker beanChecker = (BeanChecker) privatePluginService.getBean(
					extension.getDeclaringPluginDescriptor(), extension.getParameter("class").valueAsString());
				builder.add(beanChecker);
			}
			this.beanCheckers = builder.build();
		}
		return beanCheckers;
	}

	private synchronized void setupBeanLocators()
	{
		Builder<String, GuiceBeanLocator> builder = new ImmutableMap.Builder<String, GuiceBeanLocator>();
		PluginRegistry registry = getManager().getRegistry();
		ExtensionPoint point = registry.getExtensionPoint(PLUGIN_ID, "module"); //$NON-NLS-1$
		Collection<Extension> extensions = point.getConnectedExtensions();
		for( Extension extension : extensions )
		{
			String extPluginId = extension.getDeclaringPluginDescriptor().getId();
			GuiceBeanLocator locator = guiceLocators.get(extPluginId);
			if( locator == null )
			{
				locator = new GuiceBeanLocator(extPluginId, extension);
			}
			privatePluginService.setPluginBeanLocator(extPluginId, locator);
			builder.put(extPluginId, locator);
		}
		guiceLocators = builder.build();
	}

	@Override
	public void registryChanged(RegistryChangeData change)
	{
		setupBeanLocators();
	}
}
