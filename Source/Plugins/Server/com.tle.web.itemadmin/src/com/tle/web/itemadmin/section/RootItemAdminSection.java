package com.tle.web.itemadmin.section;

import javax.inject.Inject;

import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.itemadmin.ItemAdminPrivilegeTreeProvider;
import com.tle.web.search.base.ContextableSearchSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.template.section.event.BlueBarEvent;
import com.tle.web.template.section.event.BlueBarEventListener;

@SuppressWarnings("nls")
public class RootItemAdminSection extends ContextableSearchSection<ContextableSearchSection.Model>
	implements
		BlueBarEventListener
{
	public static final String ITEMADMINURL = "/access/itemadmin.do";

	@Inject
	private ItemAdminPrivilegeTreeProvider securityProvider;

	@PlugKey("title")
	private static Label title;

	@ViewFactory
	private FreemarkerFactory view;

	@Override
	protected String getSessionKey()
	{
		return "itemadminContext";
	}

	@Override
	public Label getTitle(SectionInfo info)
	{
		return title;
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		securityProvider.checkAuthorised();
		return super.renderHtml(context);
	}

	@Override
	public void addBlueBarResults(RenderContext context, BlueBarEvent event)
	{
		event.addHelp(view.createResult("help.ftl", this));
	}
}
