package com.tle.web.workflow.tasks;

import java.util.Collections;

import javax.inject.Inject;

import com.tle.core.guice.Bind;
import com.tle.core.services.item.FreeTextService;
import com.tle.core.user.CurrentUser;
import com.tle.core.user.UserState;
import com.tle.core.workflow.freetext.TaskListSearch;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.IconLabel;
import com.tle.web.sections.result.util.IconLabel.Icon;
import com.tle.web.sections.result.util.NumberLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.template.section.AbstractCachedTopbarLink;

@Bind
public class TasksTopbarLink extends AbstractCachedTopbarLink
{
	private static final String SESSION_KEY = TasksTopbarLink.class.getName();

	@PlugKey("topbar.link.tasks")
	public static Label LINK_TITLE;

	@Inject
	private FreeTextService freeTextService;

	@Override
	public int getCount()
	{
		return freeTextService.countsFromFilters(Collections.singletonList(new TaskListSearch()))[0];
	}

	@Override
	public LinkRenderer getLink()
	{
		UserState us = CurrentUser.getUserState();
		if( us.isGuest() || us.isSystem() )
		{
			return null;
		}
		HtmlLinkState taskLink = new HtmlLinkState(new SimpleBookmark("access/tasklist.do"));
		int taskCount = getCachedValue();
		if( taskCount == 0 )
		{
			taskLink.setDisabled(true);
			taskLink.addClass("disabled");
		}
		else
		{
			taskLink.addClass("embiggen");
		}
		taskLink.setLabel(new IconLabel(Icon.FLAG, new NumberLabel(taskCount)));
		taskLink.setTitle(LINK_TITLE);
		return new LinkRenderer(taskLink);
	}

	@Override
	public String getSessionKey()
	{
		return SESSION_KEY + CurrentUser.getSessionID();
	}

}
