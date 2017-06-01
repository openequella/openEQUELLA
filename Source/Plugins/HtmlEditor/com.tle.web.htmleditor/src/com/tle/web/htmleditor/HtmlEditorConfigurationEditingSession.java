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

package com.tle.web.htmleditor;

import java.io.Serializable;

import com.tle.common.htmleditor.HtmlEditorConfiguration;

/**
 * @author Aaron
 */
public class HtmlEditorConfigurationEditingSession implements Serializable
{
	private String sessionId;
	private HtmlEditorConfiguration config;
	private boolean dirty;

	public String getSessionId()
	{
		return sessionId;
	}

	public void setSessionId(String sessionId)
	{
		this.sessionId = sessionId;
	}

	public HtmlEditorConfiguration getConfig()
	{
		return config;
	}

	public void setConfig(HtmlEditorConfiguration config)
	{
		this.config = config;
	}

	public boolean isDirty()
	{
		return dirty;
	}

	public void setDirty(boolean dirty)
	{
		this.dirty = dirty;
	}
}
