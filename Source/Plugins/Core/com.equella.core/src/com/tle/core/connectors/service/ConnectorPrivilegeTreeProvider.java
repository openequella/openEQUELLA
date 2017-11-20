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

package com.tle.core.connectors.service;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.common.connectors.entity.Connector;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.entity.security.AbstractEntityPrivilegeTreeProvider;
import com.tle.core.guice.Bind;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

@Bind
@Singleton
@SuppressWarnings("nls")
public class ConnectorPrivilegeTreeProvider extends AbstractEntityPrivilegeTreeProvider<Connector>
{
	private static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(ConnectorPrivilegeTreeProvider.class);

	@Inject
	public ConnectorPrivilegeTreeProvider(ConnectorService connectorService)
	{
		super(connectorService, Node.ALL_CONNECTORS, resources.key("securitytree.allconnectors"), Node.CONNECTOR,
			resources.key("securitytree.targetallconnectors"));
	}

	@Override
	protected Connector createEntity()
	{
		return new Connector();
	}
}
