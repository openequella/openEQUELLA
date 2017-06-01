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

package com.tle.core.url;

import com.tle.core.events.ApplicationEvent;

/**
 * @author Nicholas Read
 */
public class URLEvent extends ApplicationEvent<URLListener>
{
	private static final long serialVersionUID = 1L;

	public static enum URLEventType
	{
		URL_WARNING, URL_DISABLED
	}

	private final URLEventType type;
	private final String url;

	public URLEvent(URLEventType type, String url)
	{
		super(PostTo.POST_TO_SELF_SYNCHRONOUSLY);
		this.type = type;
		this.url = url;
	}

	public URLEventType getType()
	{
		return type;
	}

	public String getUrl()
	{
		return url;
	}

	@Override
	public Class<URLListener> getListener()
	{
		return URLListener.class;
	}

	@Override
	public void postEvent(URLListener listener)
	{
		listener.urlEvent(this);
	}
}
