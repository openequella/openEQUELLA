package com.tle.core.events;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.tle.common.NamedThreadFactory;

public final class DefaultExecutor
{
	private static final int MAX_THREADS = 150;

	public static final ExecutorService executor;

	private DefaultExecutor()
	{
		throw new Error();
	}

	static
	{
		ThreadPoolExecutor tpe = new ThreadPoolExecutor(MAX_THREADS, MAX_THREADS, 60L, TimeUnit.SECONDS,
			new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory("DefaultExecutor.executor"));
		tpe.allowCoreThreadTimeOut(true);
		executor = tpe;
	}
}
