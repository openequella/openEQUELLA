package com.tle.core.reporting.web;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.entity.report.Report;
import com.tle.core.guice.Bind;
import com.tle.core.reporting.ReportingService;
import com.tle.core.services.user.UserSessionService;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.template.section.MenuContributor;

@Bind
@Singleton
@SuppressWarnings("nls")
public class ReportingMenuContributor implements MenuContributor
{
	private static final Label LABEL_KEY = new KeyLabel(ResourcesService.getResourceHelper(
		ReportingMenuContributor.class).key("menu.reporting"));
	private static final String ICON_PATH = ResourcesService.getResourceHelper(ReportingMenuContributor.class).url(
		"images/reports-menu-icon.png");
	private static final String SESSION_KEY = "REPORTING-MENU";

	@Inject
	private ReportingService reportingService;
	@Inject
	private UserSessionService userSessionService;

	@Override
	public void clearCachedData()
	{
		userSessionService.removeAttribute(SESSION_KEY);
	}

	@Override
	public List<MenuContribution> getMenuContributions(SectionInfo info)
	{
		Boolean show = userSessionService.getAttribute(SESSION_KEY);

		if( show == null )
		{
			show = false;
			List<Report> reports = reportingService.enumerateExecutable();
			for( Report report : reports )
			{
				if( !report.isHideReport() )
				{
					show = true;
					break;
				}
			}
			userSessionService.setAttribute(SESSION_KEY, show);
		}

		if( show )
		{
			// TODO: We should be generating a bookmark to the section rather
			// than hard-coding the URL

			HtmlLinkState hls = new HtmlLinkState(new SimpleBookmark("access/reports.do"));
			hls.setLabel(LABEL_KEY);
			MenuContribution mc = new MenuContribution(hls, ICON_PATH, 30, 20);
			return Collections.singletonList(mc);
		}
		else
		{
			return Collections.emptyList();
		}
	}
}
