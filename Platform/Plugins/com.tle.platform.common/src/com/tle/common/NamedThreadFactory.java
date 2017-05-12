package com.tle.common;

import java.util.concurrent.Callable;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * @author Aaron
 */
public class NamedThreadFactory implements ThreadFactory
{
	private final Cache<String, AtomicInteger> threadNumber = CacheBuilder.newBuilder().concurrencyLevel(20).build();
	private final String name;
	private static final Builder builder = new Builder();

	public NamedThreadFactory(String name)
	{
		this.name = name;
	}

	@Override
	public Thread newThread(Runnable r)
	{
		try
		{
			final AtomicInteger ctr = threadNumber.get(name, builder);
			return new Thread(r, name + "-" + ctr.getAndIncrement());
		}
		catch( Throwable t )
		{
			throw Throwables.propagate(t);
		}
	}

	private static class Builder implements Callable<AtomicInteger>
	{
		@Override
		public AtomicInteger call() throws Exception
		{
			return new AtomicInteger(1);
		}
	}
}
