/*
 * Copyright 2019 Apereo
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

package com.tle.web.service.oai.legacy;

import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import ORG.oclc.oai.server.OAIHandler;

import com.google.inject.MembersInjector;
import com.tle.core.guice.Bind;

@Deprecated
@Bind
@Singleton
public class OAIServletLegacy extends OAIHandler
{
	@Inject
	private OAIProperties properties;
	@Inject
	private MembersInjector<OAICatalog> catalogInjector;
	@Inject
	private MembersInjector<XMLRecordFactory> recordInjector;

	@Override
	public void init(ServletConfig config) throws ServletException
	{
		Properties props = properties.getProperties();
		config.getServletContext().setAttribute(OAIHandler.PROPERTIES_SERVLET_CONTEXT_ATTRIBUTE, props);

		super.init(config);

		OAICatalog catalog = (OAICatalog) getAttributes("").get("OAIHandler.catalog"); //$NON-NLS-1$ //$NON-NLS-2$
		catalogInjector.injectMembers(catalog);
		recordInjector.injectMembers((XMLRecordFactory) catalog.getRecordFactory());
	}
}
