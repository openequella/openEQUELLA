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

package com.tle.web.contribute.navigation;

import java.util.Map;

import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.web.navigation.BreadcrumbProvider;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;

/**
 * I'm not sure com.tle.web.sections.equella should depend on
 * com.tle.web.viewurl... otherwise I would have put this class in that plugin.
 * 
 * @author aholland
 */
@Bind
@Singleton
public class ContributeBreadcrumbProvider implements BreadcrumbProvider
{
	@PlugKey("breadcrumb.title")
	private static Label BREADCRUMB_TITLE;

	@SuppressWarnings("nls")
	private static Label LABEL = new KeyLabel(ResourcesService.getResourceHelper(ContributeBreadcrumbProvider.class)
		.key("breadcrumbForContributionSelector"));

	@Override
	public TagState getBreadcrumb(SectionInfo info, Map<String, ?> params)
	{
		HtmlLinkState con = new HtmlLinkState((info.createForward("/access/contribute.do").getPublicBookmark()));
		con.setLabel(LABEL);
		con.setRel("parent");
		con.setTitle(BREADCRUMB_TITLE);
		return con;
	}
}
