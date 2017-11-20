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

package com.tle.web.connectors.manage;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.connectors.service.ConnectorService;
import com.tle.core.guice.Bind;
import com.tle.web.connectors.service.ConnectorManagementPrivilegeTreeProvider;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.template.section.AbstractCachedMenuContributor;

@Bind
@Singleton
public class ConnectorManagementMenuContributor extends AbstractCachedMenuContributor<Boolean>
{
	@PlugKey("connector.manage.menu")
	private static Label LABEL_MENU;
	@PlugURL("images/icons/manage-external-resources.png")
	private static String URL_ICON;

	private static final String KEY = ConnectorManagementMenuContributor.class.getName();

	@Inject
	private ConnectorManagementPrivilegeTreeProvider securityProvider;
	@Inject
	private ConnectorService connectorService;

	@Override
	@SuppressWarnings("nls")
	protected MenuContribution getContribution(SectionInfo info, Boolean hasManageTasks)
	{
		if( hasManageTasks )
		{
			return new MenuContribution(
				new HtmlLinkState(LABEL_MENU, new SimpleBookmark("access/manageconnectors.do")), URL_ICON, 30, 6);
		}
		return null;
	}

	@Override
	protected Boolean getCachedObject(SectionInfo info)
	{
		return Boolean.valueOf(securityProvider.isAuthorised() && !connectorService.listForViewing().isEmpty());
	}

	@Override
	protected String getSessionKey()
	{
		return KEY;
	}

}
