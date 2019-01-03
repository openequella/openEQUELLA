/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.upgrademanager.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@SuppressWarnings("nls")
public class AjaxStateImpl implements AjaxState
{
	private static final Log LOGGER = LogFactory.getLog(AjaxState.class);

	protected final HashMap<String, List<AjaxMessage>> map = new HashMap<String, List<AjaxMessage>>();

	@Override
	public void addErrorRaw(String id, String message)
	{
		this.addMessage(id, new AjaxMessage("error", message));
	}

	@Override
	public synchronized void addError(String id, String message)
	{
		this.addMessage(id, new AjaxMessage("error", "<b>AN ERROR OCCURRED: </b>" + message));
	}

	@Override
	public synchronized void addBasic(String id, String message)
	{
		this.addMessage(id, new AjaxMessage("basic", message));
	}

	@Override
	public synchronized void start(String id, String message)
	{
		this.addMessage(id, new AjaxMessage("heading", message));
	}

	@Override
	public synchronized void start(String id)
	{
		// nothing
	}

	@Override
	public synchronized void addHeading(String id, String message)
	{
		this.addMessage(id, new AjaxMessage("heading", message));
	}

	@Override
	public synchronized void addConsole(String id, String message)
	{
		this.addMessage(id, new AjaxMessage("console", message));
	}

	@Override
	public synchronized void finish(String id, String message, String redirect)
	{
		AjaxMessage finishMsg = new AjaxMessage("finish", message); //$NON-NLS-1$
		finishMsg.setRedirect(redirect);
		addMessage(id, finishMsg);
	}

	public synchronized void addMessage(String id, AjaxMessage message)
	{
		List<AjaxMessage> ls = map.get(id);
		if( ls == null )
		{
			ls = new ArrayList<AjaxMessage>();
		}

		ls.add(message);
		LOGGER.info(message.getMessage());

		map.put(id, ls);
	}

	@Override
	public synchronized List<AjaxMessage> getListOfAllMessages(String id)
	{
		List<AjaxMessage> ls = map.remove(id);
		if( ls == null )
		{
			ls = new ArrayList<AjaxMessage>();
		}
		return ls;
	}
}
