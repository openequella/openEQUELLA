/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.web.template.section;

import com.google.inject.Inject;
import com.tle.core.system.SystemConfigService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.GenericNamedResult;
import com.tle.web.sections.render.HtmlRenderer;

@SuppressWarnings("nls")
public class ServerMessageSection extends AbstractPrototypeSection<ServerMessageSection.ServerMessageModel>
	implements
		HtmlRenderer
{
	@Inject
	private SystemConfigService systemConfigService;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( !systemConfigService.isSystemSchemaUp() || !systemConfigService.isServerMessageEnabled() )
		{
			return null;
		}

		getModel(context).setServerMessage(systemConfigService.getServerMessage());
		return new GenericNamedResult("servermessage", viewFactory.createResult("servermessage.ftl", context));
	}

	@Override
	public Class<ServerMessageModel> getModelClass()
	{
		return ServerMessageModel.class;
	}

	public static class ServerMessageModel
	{
		private String serverMessage;

		public String getServerMessage()
		{
			return serverMessage;
		}

		public void setServerMessage(String serverMessage)
		{
			this.serverMessage = serverMessage;
		}
	}
}
