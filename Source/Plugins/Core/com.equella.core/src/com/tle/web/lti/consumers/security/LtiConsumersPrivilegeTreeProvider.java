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

package com.tle.web.lti.consumers.security;

import javax.inject.Inject;

import com.tle.common.lti.consumers.entity.LtiConsumer;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.entity.security.AbstractEntityPrivilegeTreeProvider;
import com.tle.core.guice.Bind;
import com.tle.core.lti.consumers.service.LtiConsumerService;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

@Bind
public class LtiConsumersPrivilegeTreeProvider extends AbstractEntityPrivilegeTreeProvider<LtiConsumer>
{
	private static final PluginResourceHelper RESOURCES = ResourcesService
		.getResourceHelper(LtiConsumersPrivilegeTreeProvider.class);

	@Inject
	protected LtiConsumersPrivilegeTreeProvider(LtiConsumerService consumerService)
	{
		super(consumerService, Node.ALL_LTI_CONSUMERS, RESOURCES.key("securitytree.alllticonsumers"), Node.LTI_CONSUMER,
			RESOURCES.key("securitytree.targeralllticonsumers"));
	}

	@Override
	protected LtiConsumer createEntity()
	{
		return new LtiConsumer();
	}

}
