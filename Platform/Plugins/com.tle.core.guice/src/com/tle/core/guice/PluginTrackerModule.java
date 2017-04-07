package com.tle.core.guice;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.util.Comparator;

import javax.inject.Inject;

import org.java.plugin.registry.Extension;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.util.Types;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;

public abstract class PluginTrackerModule extends AbstractModule
{
	public static class TrackerProvider<T> implements Provider<PluginTracker<T>>
	{
		@Inject
		private PluginService pluginService;
		private String extensionPoint;
		private String beanParameter;
		private String idParam;
		private Class<?> moduleClass;
		private Comparator<Extension> comparator;

		public TrackerProvider(Class<?> moduleClass, String extensionPoint, String beanParameter)
		{
			this.moduleClass = moduleClass;
			this.extensionPoint = extensionPoint;
			this.beanParameter = beanParameter;
		}

		@Override
		public PluginTracker<T> get()
		{
			return new PluginTracker<T>(pluginService, moduleClass, extensionPoint, idParam, comparator)
				.setBeanKey(beanParameter);
		}

		public TrackerProvider<T> setIdParam(String idParam)
		{
			this.idParam = idParam;
			return this;
		}

		public TrackerProvider<T> orderByParameter(String param)
		{
			return orderByParameter(param, false);
		}

		public TrackerProvider<T> orderByParameter(String param, boolean reverse)
		{
			return orderByParameter(param, reverse, false);
		}

		public TrackerProvider<T> orderByParameter(String param, boolean reverse, boolean nullsLast)
		{
			comparator = new PluginTracker.ExtensionParamComparator(param, nullsLast);
			if( reverse )
			{
				comparator = new InverseComparator<Extension>(comparator);
			}
			return this;
		}

		private static class InverseComparator<T> implements Comparator<T>
		{
			private final Comparator<T> delegate;

			public InverseComparator(Comparator<T> delegate)
			{
				this.delegate = delegate;
			}

			@Override
			public int compare(T o1, T o2)
			{
				return -delegate.compare(o1, o2);
			}
		}
	}

	protected <T> TrackerProvider<T> bindTracker(Class<T> clazz, String extensionPoint, String beanParameter)
	{
		return bindTracker((java.lang.reflect.Type) clazz, null, extensionPoint, beanParameter);
	}

	protected <T> TrackerProvider<T> bindTracker(java.lang.reflect.Type typeParam, String extensionPoint,
		String beanParameter)
	{
		return bindTracker(typeParam, null, extensionPoint, beanParameter);
	}

	protected <T> TrackerProvider<T> bindTracker(Class<T> clazz, Annotation annotatedWith, String extensionPoint,
		String beanParameter)
	{
		return bindTracker((java.lang.reflect.Type) clazz, annotatedWith, extensionPoint, beanParameter);
	}

	protected <T> TrackerProvider<T> bindTracker(java.lang.reflect.Type typeParam, Annotation annotatedWith,
		String extensionPoint, String beanParameter)
	{
		ParameterizedType type = Types.newParameterizedType(PluginTracker.class, typeParam);
		TrackerProvider<T> trackerProvider = new TrackerProvider<T>(getClass(), extensionPoint, beanParameter);
		@SuppressWarnings("unchecked")
		TypeLiteral<PluginTracker<T>> typeLiteral = (TypeLiteral<PluginTracker<T>>) TypeLiteral.get(type);
		LinkedBindingBuilder<PluginTracker<T>> bindingBuilder = bind(typeLiteral);
		if( annotatedWith != null )
		{
			bindingBuilder = ((AnnotatedBindingBuilder<PluginTracker<T>>) bindingBuilder).annotatedWith(annotatedWith);
		}
		bindingBuilder.toProvider(trackerProvider).in(Scopes.SINGLETON);
		return trackerProvider;
	}
}
