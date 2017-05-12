package com.tle.core.plugins;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import com.google.common.base.Throwables;

public abstract class AbstractBeanLocatorCallable<V> implements Callable<V>
{
	private Map<BlockingQueue<Object>, BlockingQueue<Object>> waitingList = new IdentityHashMap<BlockingQueue<Object>, BlockingQueue<Object>>();
	protected boolean submitted;

	protected final PrivatePluginBeanLocator locator;
	private boolean finished;

	public AbstractBeanLocatorCallable(PrivatePluginBeanLocator locator)
	{
		this.locator = locator;
	}

	public synchronized void submit(ExecutorService service)
	{
		if( !submitted )
		{
			submitted = true;
			service.submit(this);
		}
	}

	public synchronized void addWaiter(BlockingQueue<Object> queue)
	{
		if( finished )
		{
			queue.add(this);
		}
		else
		{
			waitingList.put(queue, queue);
		}
	}

	public synchronized void finished()
	{
		finished = true;
		locator.clearCallable();
		for( BlockingQueue<Object> waiter : waitingList.keySet() )
		{
			waiter.add(this);
		}
	}

	@Override
	public V call() throws Exception
	{
		try
		{
			return doWork();
		}
		catch( Throwable t )
		{
			locator.setThrowable(t);
			throw Throwables.propagate(t);
		}
		finally
		{
			finished();
		}
	}

	protected abstract V doWork() throws Exception;
}