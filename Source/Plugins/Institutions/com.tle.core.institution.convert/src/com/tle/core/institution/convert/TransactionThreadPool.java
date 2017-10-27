/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.institution.convert;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.tle.core.hibernate.CurrentDataSource;
import com.tle.core.hibernate.DataSourceHolder;
import com.tle.common.usermanagement.user.AuthenticatedThread;

public class TransactionThreadPool
{
	private List<TransactionThread> freeThreads = new LinkedList<TransactionThread>();
	private List<TransactionThread> inUseThreads = new LinkedList<TransactionThread>();
	private boolean closing;
	private volatile Throwable throwable;
	private Converter converter;
	private DataSourceHolder dataSource;

	public TransactionThreadPool(Converter converter, int poolSize)
	{
		this.converter = converter;
		for( int i = 0; i < poolSize; i++ )
		{
			freeThreads.add(new TransactionThread());
		}
		dataSource = CurrentDataSource.get();
	}

	public synchronized void doInTransaction(Runnable runnable)
	{
		while( freeThreads.isEmpty() && throwable == null )
		{
			try
			{
				wait();
			}
			catch( InterruptedException e )
			{
				// who cares
			}
		}
		if( throwable != null )
		{
			return;
		}
		TransactionThread thread = freeThreads.remove(0);
		inUseThreads.add(thread);
		thread.setRunnable(runnable);
	}

	private void reportError(Throwable t)
	{
		if( throwable == null )
		{
			this.throwable = t;
		}
	}

	private synchronized boolean threadFinished(TransactionThread thread)
	{
		inUseThreads.remove(thread);
		freeThreads.add(thread);
		notifyAll();
		return closing;
	}

	private class TransactionThread extends AuthenticatedThread
	{
		private Runnable runnable;
		private boolean end;

		@Override
		public synchronized void doRun()
		{
			CurrentDataSource.set(dataSource);
			while( !end )
			{
				while( runnable == null && !end )
				{
					try
					{
						wait();
					}
					catch( InterruptedException e )
					{
						// Don't care
					}
				}

				try
				{
					if( !end )
					{
						converter.doInTransaction(runnable);
					}
				}
				catch( Exception t )
				{
					reportError(t);
				}
				finally
				{
					runnable = null;
					end = threadFinished(this);
				}
			}
		}

		public synchronized void end()
		{
			end = true;
			interrupt();
		}

		public synchronized void setRunnable(Runnable runnable)
		{
			this.runnable = runnable;
			if( !isAlive() )
			{
				start();
			}
			notifyAll();
		}
	}

	public void close()
	{
		closing = true;
		List<TransactionThread> toEnd = new ArrayList<TransactionThread>();
		synchronized( this )
		{
			if( throwable != null )
			{
				toEnd.addAll(inUseThreads);
			}
			toEnd.addAll(freeThreads);
		}
		for( TransactionThread thread : toEnd )
		{
			thread.end();
		}
		synchronized( this )
		{
			while( inUseThreads.size() > 0 )
			{
				try
				{
					wait();
				}
				catch( InterruptedException e )
				{
					// don't care
				}
			}
			if( throwable != null )
			{
				if( throwable instanceof RuntimeException )
				{
					throw (RuntimeException) throwable;
				}
				throw new RuntimeException(throwable);
			}
		}
	}

	public boolean hasException()
	{
		return throwable != null;
	}

}
