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

package com.tle.web.cloud.view.actions;

import javax.inject.Inject;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.web.cloud.view.section.CloudItemSectionInfo;
import com.tle.web.cloud.viewable.CloudViewItemLinkFactory;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.viewitem.sharing.AbstractShareWithOthersSection;

@SuppressWarnings("nls")
@Bind
public class CloudShareWithOthersContentSection extends AbstractShareWithOthersSection
{
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Inject
	private CloudViewItemLinkFactory linkFactory;

	@Override
	public boolean canView(SectionInfo info)
	{
		return true;
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		return viewFactory.createResult("actions/dialog/sharecloudwithothers.ftl", this);
	}

	@Override
	protected String createEmail(SectionInfo info)
	{
		return buildEmail(info);
	}

	private String buildEmail(SectionInfo info)
	{
		// TODO this could be further refactored
		StringBuilder email = new StringBuilder();
		CloudItemSectionInfo iinfo = CloudItemSectionInfo.getItemInfo(info);

		email.append(s("intro", getUser(CurrentUser.getDetails())));

		email.append(messageField.getValue(info));
		email.append("\n\n");
		email.append(s("item.name", CurrentLocale.get(iinfo.getViewableItem().getItem().getName())));
		email.append(s("item.link", linkFactory.createCloudViewLink(iinfo.getItemId()).getHref()));
		email.append(s("item.version", iinfo.getViewableItem().getItem().getVersion()));
		email.append("\n");
		email.append(s("outro"));

		return email.toString();
	}

}
