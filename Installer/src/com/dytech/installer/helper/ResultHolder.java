package com.dytech.installer.helper;

/**
 * This class provides the passing of a result (an <code>Object</code>), but
 * with synchronisation. Using <code>getResult</code> will put the caller in to
 * a blocked state until a result has been set.
 * 
 * @author Nicholas Read
 */
public class ResultHolder
{
	/**
	 * Holds the result.
	 */
	protected Object result;

	/**
	 * Indicates whether the result has been set.
	 */
	protected boolean noResult;

	/**
	 * Constructs a ResultHolder.
	 */
	public ResultHolder()
	{
		result = null;
		noResult = true;
	}

	/**
	 * Set the result and notify all waiting parties.
	 * 
	 * @param result The result to hold.
	 */
	public synchronized void setResult(Object result)
	{
		this.result = result;
		noResult = false;
		notifyAll();
	}

	/**
	 * Get the result. Will block until a result has been set.
	 * 
	 * @return The result as an <code>Object</cdoe>.
	 */
	public synchronized Object getResult()
	{
		while( noResult )
		{
			try
			{
				wait();
			}
			catch( InterruptedException e )
			{
				// We don't care
			}
		}

		return result;
	}

	/**
	 * Checks if a result has been set yet.
	 * 
	 * @return True if result is set.
	 */
	public synchronized boolean isResult()
	{
		return !noResult;
	}
}