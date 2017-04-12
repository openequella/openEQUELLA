package com.tle.core.events;

public interface EventExecutor
{
	Runnable createRunnable(long institutionId, Runnable runnable);
}
