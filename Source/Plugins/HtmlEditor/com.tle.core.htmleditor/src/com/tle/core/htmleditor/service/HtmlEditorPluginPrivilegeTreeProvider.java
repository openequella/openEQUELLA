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

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.common.htmleditor.beans.HtmlEditorPlugin;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.entity.security.AbstractEntityPrivilegeTreeProvider;
import com.tle.core.guice.Bind;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

@Bind
@Singleton
@SuppressWarnings("nls")
public class HtmlEditorPluginPrivilegeTreeProvider extends AbstractEntityPrivilegeTreeProvider<HtmlEditorPlugin>
{
	private static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(HtmlEditorPluginPrivilegeTreeProvider.class);

	@Inject
	public HtmlEditorPluginPrivilegeTreeProvider(HtmlEditorPluginService htmleditorService)
	{
		super(htmleditorService, Node.ALL_HTMLEDITOR_PLUGINS, resources.key("securitytree.allhtmleditorplugin"),
			Node.HTMLEDITOR_PLUGIN, resources.key("securitytree.htmleditorplugin"));
	}

	@Override
	protected HtmlEditorPlugin createEntity()
	{
		return new HtmlEditorPlugin();
	}
}
