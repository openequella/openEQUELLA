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

package com.tle.web.cloud.search.section;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tle.beans.item.ItemId;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.cloud.beans.converted.CloudItem;
import com.tle.core.cloud.service.CloudSearchResults;
import com.tle.core.cloud.service.CloudService;
import com.tle.core.guice.Bind;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.web.cloud.event.CloudSearchEvent;
import com.tle.web.cloud.viewable.CloudViewItemLinkFactory;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.search.actions.AbstractShareSearchQuerySection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.InfoBookmark;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.SectionRenderable;

@SuppressWarnings("nls")
@Bind
public class CloudShareSearchQuerySection extends AbstractShareSearchQuerySection implements HtmlRenderer
{
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Inject
	private CloudService cloudService;
	@Inject
	private CloudViewItemLinkFactory cloudLinkFactory;

	@Override
	public SectionRenderable renderHtml(RenderEventContext context)
	{
		getModel(context).setShowEmail(emailService.hasMailSettings());
		InfoBookmark bookmark = rootSearch.getPermanentUrl(context);
		setupUrl(bookmark, context);
		return viewFactory.createResult("actions/dialog/sharecloudsearchquery.ftl", this);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.setLayout(id, SearchResultsActionsSection.AREA_SHARE);
	}

	@Override
	public String createEmail(SectionInfo info)
	{
		CloudSearchEvent event = (CloudSearchEvent) getSearchResultsSection().createSearchEvent(info);
		info.processEvent(event);

		return buildEmail(event);
	}

	private String buildEmail(CloudSearchEvent event)
	{
		StringBuilder email = new StringBuilder();

		email.append(s("intro", getUser(CurrentUser.getDetails())));
		email.append(s("query", Strings.nullToEmpty(event.getCloudSearch().getQuery())));
		for( Map<String, String> result : getResults(event) )
		{
			email.append(result.get("name"));
			email.append(result.get("link"));
			email.append(result.get("version"));
			email.append("\n");
		}
		email.append(s("outro"));

		return email.toString();
	}

	private List<Map<String, String>> getResults(CloudSearchEvent event)
	{
		final CloudSearchResults results = cloudService.search(event.getCloudSearch(), 0, RESULTS_CAP);
		final List<Map<String, String>> shareResults = Lists.newArrayList();
		for( CloudItem cloudItem : results.getResults() )
		{
			final Map<String, String> res = Maps.newHashMap();
			res.put("name", s("item.name", CurrentLocale.get(cloudItem.getName(), cloudItem.getUuid())));
			final ItemId itemId = cloudItem.getItemId();
			final String href = cloudLinkFactory.createCloudViewLink(itemId).getHref();
			res.put("link", s("item.link", href));
			res.put("version", s("item.version", Integer.toString(itemId.getVersion())));
			shareResults.add(res);
		}
		return shareResults;
	}
}
