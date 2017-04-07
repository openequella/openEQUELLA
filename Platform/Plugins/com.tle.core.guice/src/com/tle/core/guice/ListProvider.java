package com.tle.core.guice;

import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Provider;

public class ListProvider<T> implements Provider<List<T>>
{
	@Inject
	private Injector injector;
	private Binder binder;

	private final List<Class<? extends T>> clazzes = Lists.newArrayList();

	public ListProvider(Binder binder)
	{
		this.binder = binder;
	}

	public ListProvider(Binder binder, List<Class<? extends T>> clazzes)
	{
		this.binder = binder;
		this.clazzes.addAll(clazzes);
		for( Class<? extends T> clazz : clazzes )
		{
			binder.bind(clazz);
		}
	}

	public void add(Class<? extends T> clazz)
	{
		binder.bind(clazz);
		clazzes.add(clazz);
	}

	@Override
	public List<T> get()
	{
		List<T> list = Lists.newArrayList();
		for( Class<? extends T> clazz : clazzes )
		{
			T instance = injector.getInstance(clazz);
			list.add(instance);
		}
		return list;
	}

}
