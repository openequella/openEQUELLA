package com.tle.core.hibernate;

public abstract class CurrentDataSource
{
	private static ThreadLocal<DataSourceHolder> threadLocal = new ThreadLocal<DataSourceHolder>();

	public static DataSourceHolder get()
	{
		return threadLocal.get();
	}

	public static void set(DataSourceHolder dataSource)
	{
		threadLocal.set(dataSource);
	}

	public static void remove()
	{
		threadLocal.remove();
	}
}
