package com.tle.web.mimetypes.search.section;

import java.util.List;

import javax.inject.Inject;

import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.mimetypes.MimeSearchPrivilegeTreeProvider;
import com.tle.web.search.base.ContextableSearchSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.DirectEvent;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.Label;
import com.tle.web.settings.menu.SettingsUtils;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;
import com.tle.web.template.section.event.BlueBarEvent;
import com.tle.web.template.section.event.BlueBarEventListener;

@SuppressWarnings("nls")
public class RootMimeSection extends ContextableSearchSection<ContextableSearchSection.Model>
	implements
		BlueBarEventListener
{
	@PlugURL("css/mime.css")
	private static String CSS_URL;
	@PlugKey("title")
	private static Label TITLE_LABEL;

	@Inject
	private MimeSearchPrivilegeTreeProvider securityProvider;

	@ViewFactory
	private FreemarkerFactory view;

	@Override
	protected String getSessionKey()
	{
		return "mimeContext";
	}

	@DirectEvent
	public void ensurePrivs(SectionInfo info)
	{
		securityProvider.checkAuthorised();
	}

	@Override
	protected void createCssIncludes(List<CssInclude> includes)
	{
		includes.add(CssInclude.include(CSS_URL).hasRtl().make());
		super.createCssIncludes(includes);
	}

	@Override
	public Label getTitle(SectionInfo info)
	{
		return TITLE_LABEL;
	}

	@Override
	protected void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		super.addBreadcrumbsAndTitle(info, decorations, crumbs);

		Breadcrumbs.get(info).add(SettingsUtils.getBreadcrumb());
	}

	@Override
	protected String getContentBodyClasses()
	{
		return super.getContentBodyClasses() + " mimetypes";
	}

	@Override
	public void addBlueBarResults(RenderContext context, BlueBarEvent event)
	{
		event.addHelp(view.createResult("help.ftl", this));
	}
}
