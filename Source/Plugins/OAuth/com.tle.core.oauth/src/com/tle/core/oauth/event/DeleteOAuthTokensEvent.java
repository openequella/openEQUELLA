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

package com.tle.core.oauth.event;

import java.util.List;

import com.tle.core.events.ApplicationEvent;
import com.tle.core.oauth.event.listener.DeleteOAuthTokensEventListener;

public class DeleteOAuthTokensEvent extends ApplicationEvent<DeleteOAuthTokensEventListener>
{
	private static final long serialVersionUID = 1L;

	private final List<String> tokens;

	public DeleteOAuthTokensEvent(List<String> tokens)
	{
		super(PostTo.POST_TO_ALL_CLUSTER_NODES);
		this.tokens = tokens;
	}

	public List<String> getTokens()
	{
		return tokens;
	}

	@Override
	public Class<DeleteOAuthTokensEventListener> getListener()
	{
		return DeleteOAuthTokensEventListener.class;
	}

	@Override
	public void postEvent(DeleteOAuthTokensEventListener listener)
	{
		listener.deleteOAuthTokensEvent(this);
	}
}
