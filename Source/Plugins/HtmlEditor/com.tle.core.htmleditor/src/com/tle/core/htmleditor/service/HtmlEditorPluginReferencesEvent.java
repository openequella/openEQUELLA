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

package com.tle.core.htmleditor.service;

import com.tle.common.htmleditor.beans.HtmlEditorPlugin;
import com.tle.core.entity.event.BaseEntityReferencesEvent;

public class HtmlEditorPluginReferencesEvent
	extends
		BaseEntityReferencesEvent<HtmlEditorPlugin, HtmlEditorPluginReferencesListener>
{
	private static final long serialVersionUID = 1L;

	public HtmlEditorPluginReferencesEvent(HtmlEditorPlugin client)
	{
		super(client);
	}

	@Override
	public Class<HtmlEditorPluginReferencesListener> getListener()
	{
		return HtmlEditorPluginReferencesListener.class;
	}

	@Override
	public void postEvent(HtmlEditorPluginReferencesListener listener)
	{
		listener.addHtmlEditorPluginReferencingClasses(entity, referencingClasses);
	}
}
