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

package com.tle.web.searching;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.edge.web.WebConstants;
import com.tle.core.guice.Bind;
import com.tle.core.security.TLEAclManager;
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
public class SearchMenuContributor implements MenuContributor
{
	private static final Label LABEL_KEY = new KeyLabel(ResourcesService.getResourceHelper(SearchMenuContributor.class)
		.key("searching.menu"));
	private static final String ICON_PATH = ResourcesService.getResourceHelper(SearchMenuContributor.class).url(
		"images/menu-icon-search.png");

	@Inject
	private TLEAclManager aclManager;

	@Override
	public List<MenuContribution> getMenuContributions(SectionInfo info)
	{
		if( aclManager.filterNonGrantedPrivileges(WebConstants.SEARCH_PAGE_PRIVILEGE).isEmpty() )
		{
			return Collections.emptyList();
		}

		HtmlLinkState hls = new HtmlLinkState(new SimpleBookmark("searching.do"));
		hls.setLabel(LABEL_KEY);
		MenuContribution mc = new MenuContribution(hls, ICON_PATH, 1, 20);
		return Collections.singletonList(mc);
	}

	@Override
	public void clearCachedData()
	{
		// Nothing is cached
	}
}
