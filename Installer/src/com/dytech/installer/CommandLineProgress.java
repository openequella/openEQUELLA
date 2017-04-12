/*
 * Created on Nov 9, 2004
 */
package com.dytech.installer;

import java.io.IOException;

/**
 * @author Nicholas Read
 */
public class CommandLineProgress implements Progress
{
	/*
	 * (non-Javadoc)
	 * @see com.dytech.installer.Progress#addMessage(java.lang.String)
	 */
	@Override
	public void addMessage(String msg)
	{
		System.out.println(msg);
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.installer.Progress#getCurrentAmount()
	 */
	@Override
	public int getCurrentAmount()
	{
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.installer.Progress#getCurrentMaximum()
	 */
	@Override
	public int getCurrentMaximum()
	{
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.installer.Progress#getWholeAmount()
	 */
	@Override
	public int getWholeAmount()
	{
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.installer.Progress#getWholeMaximum()
	 */
	@Override
	public int getWholeMaximum()
	{
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.installer.Progress#setCurrentAmount(int)
	 */
	@Override
	public void setCurrentAmount(int i)
	{
		// Ignore this
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.installer.Progress#setCurrentMaximum(int)
	 */
	@Override
	public void setCurrentMaximum(int maximum)
	{
		// Ignore this
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.installer.Progress#setup(java.lang.String, int)
	 */
	@Override
	public void setup(String title, int total)
	{
		// Ignore this
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.installer.Progress#setWholeAmount(int)
	 */
	@Override
	public void setWholeAmount(int i)
	{
		// Ignore this
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.installer.Progress#popupMessage(java.lang.String,
	 * java.lang.String, boolean)
	 */
	@Override
	public void popupMessage(String title, String message, boolean error)
	{
		System.out.println("-------------------------------------------------");
		System.out.println(title.toUpperCase());
		System.out.println();
		System.out.println(message);
		System.out.println();
		System.out.println("Press ENTER to continue...");

		try
		{
			System.in.read();
		}
		catch( IOException e )
		{
			// Honestly don't care...
		}
	}
}