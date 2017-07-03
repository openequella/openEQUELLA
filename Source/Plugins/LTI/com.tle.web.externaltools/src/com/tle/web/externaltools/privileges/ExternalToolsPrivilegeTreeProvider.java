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

package com.tle.web.externaltools.privileges;

import javax.inject.Inject;

import com.tle.common.externaltools.entity.ExternalTool;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.entity.security.AbstractEntityPrivilegeTreeProvider;
import com.tle.core.externaltools.service.ExternalToolsService;
import com.tle.core.guice.Bind;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

@Bind
public class ExternalToolsPrivilegeTreeProvider extends AbstractEntityPrivilegeTreeProvider<ExternalTool>
{
	private static PluginResourceHelper resources = ResourcesService
		.getResourceHelper(ExternalToolsPrivilegeTreeProvider.class);

	@SuppressWarnings("nls")
	@Inject
	protected ExternalToolsPrivilegeTreeProvider(ExternalToolsService service)
	{
		super(service, Node.ALL_EXTERNAL_TOOLS, resources.key("securitytree.alltools"), Node.EXTERNAL_TOOL,
			resources.key("securitytree.targetalltools"));
	}

	@Override
	protected ExternalTool createEntity()
	{
		return new ExternalTool();
	}
}
