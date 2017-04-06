package com.tle.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dytech.devlib.PropBagEx;

/**
 * Provides access to the global error and information messages.
 * 
 * @author Nicholas Read
 */
public final class Messages
{
	private static final Log LOGGER = LogFactory.getLog(Messages.class);
	private static final String MESSAGES_XML = "messages.xml"; //$NON-NLS-1$
	private static Messages instance;
	private PropBagEx xml;

	public static Messages getInstance()
	{
		if( instance == null )
		{
			instance = new Messages();
		}
		return instance;
	}

	private Messages()
	{
		try
		{
			xml = new PropBagEx(Messages.class.getResourceAsStream(MESSAGES_XML));
		}
		catch( Exception ex )
		{
			LOGGER.warn("Error reading messages xml", ex);
		}
	}

	public PropBagEx getError(String group)
	{
		return xml.getSubtree("/errors/" + group); //$NON-NLS-1$
	}
}
