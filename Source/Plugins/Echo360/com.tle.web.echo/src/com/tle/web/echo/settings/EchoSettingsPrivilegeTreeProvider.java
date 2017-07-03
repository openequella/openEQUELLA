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

package com.tle.web.echo.settings;

import javax.inject.Inject;

import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.echo.entity.EchoServer;
import com.tle.core.echo.service.EchoService;
import com.tle.core.entity.security.AbstractEntityPrivilegeTreeProvider;
import com.tle.core.guice.Bind;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

@Bind
@SuppressWarnings("nls")
public class EchoSettingsPrivilegeTreeProvider extends AbstractEntityPrivilegeTreeProvider<EchoServer>
{
	private static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(EchoSettingsPrivilegeTreeProvider.class);

	@Inject
	protected EchoSettingsPrivilegeTreeProvider(EchoService echoService)
	{
		super(echoService, Node.ALL_ECHOS, resources.key("securitytree.echoservers"), Node.ECHO,
			resources.key("securitytree.targetallechoservers"));
	}

	@Override
	protected EchoServer createEntity()
	{
		return new EchoServer();
	}
}