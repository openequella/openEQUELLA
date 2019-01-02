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

package com.tle.web.service.oai;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import ORG.oclc.oai.server.OAIHandler;
import ORG.oclc.oai.server.catalog.AbstractCatalog;

import com.google.inject.MembersInjector;
import com.tle.core.guice.Bind;

@Bind
@Singleton
public class OAIServlet extends OAIHandler
{
	@Inject
	private OAIProperties properties;
	@Inject
	private MembersInjector<OAICatalog> oaiInjector;
	@Inject
	private MembersInjector<XMLRecordFactory> recordInjector;

	@Override
	@SuppressWarnings("nls")
	public void init(ServletConfig config) throws ServletException
	{
		config.getServletContext().setAttribute(OAIHandler.PROPERTIES_SERVLET_CONTEXT_ATTRIBUTE, properties);

		super.init(config);

		AbstractCatalog catalog = (AbstractCatalog) getAttributes("").get("OAIHandler.catalog");
		oaiInjector.injectMembers((OAICatalog) catalog);
		recordInjector.injectMembers((XMLRecordFactory) catalog.getRecordFactory());
	}
}
