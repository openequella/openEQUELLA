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

import java.io.InputStream;
import java.util.List;

import com.tle.common.htmleditor.beans.HtmlEditorPlugin;
import com.tle.core.entity.service.AbstractEntityService;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public interface HtmlEditorPluginService extends AbstractEntityService<HtmlEditorPluginEditingBean, HtmlEditorPlugin>
{
	String ENTITY_TYPE = "HTMLEDITOR_PLUGIN";

	HtmlEditorPlugin getByPluginId(String pluginId);

	/**
	 * Closes the zipStream when it's done
	 * 
	 * @param zipStream
	 * @throws InvalidHtmlEditorPluginException
	 */
	void uploadPlugin(InputStream zipStream) throws InvalidHtmlEditorPluginException;

	List<HtmlEditorPlugin> enumerateForType(String type);
}
