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

package com.tle.web.notification.topbar;

import java.util.Collections;

import javax.inject.Inject;

import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.UserState;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.guice.Bind;
import com.tle.core.notification.standard.indexer.NotificationSearch;
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
public class NotificationTopbarLink extends AbstractCachedTopbarLink
{
	private static final String SESSION_KEY = NotificationTopbarLink.class.getName();

	@PlugKey("topbar.link.notifications")
	public static Label LINK_TITLE;

	@Inject
	private FreeTextService freeTextService;

	@Override
	public int getCount()
	{
		return freeTextService.countsFromFilters(Collections.singletonList(new NotificationSearch()))[0];
	}

	@Override
	public LinkRenderer getLink()
	{
		UserState us = CurrentUser.getUserState();
		if( us.isGuest() || us.isSystem() )
		{
			return null;
		}
		HtmlLinkState notificationLink = new HtmlLinkState(new SimpleBookmark("access/notifications.do"));
		int count = getCachedValue();
		if( count == 0 )
		{
			notificationLink.setDisabled(true);
			notificationLink.addClass("disabled");
		}
		notificationLink.setLabel(new IconLabel(Icon.BELL, new NumberLabel(count)));
		notificationLink.setTitle(LINK_TITLE);
		return new LinkRenderer(notificationLink);
	}

	@Override
	public String getSessionKey()
	{
		return SESSION_KEY + CurrentUser.getSessionID();
	}

}
