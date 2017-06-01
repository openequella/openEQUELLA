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
