/*
 * Created on 25/01/2006
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
