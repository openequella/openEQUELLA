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

package com.tle.web.settings.menu;

import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.SimpleBookmark;

@SuppressWarnings("nls")
public final class SettingsUtils
{
	private static final KeyLabel BREADCRUMB_LABEL = new KeyLabel(ResourcesService.getResourceHelper(
		SettingsUtils.class).key("breadcrumb"));

	public static final SimpleBookmark SETTINGS_BOOKMARK = new SimpleBookmark("access/settings.do");

	private static final KeyLabel BREADCRUMB_TITLE = new KeyLabel(ResourcesService.getResourceHelper(
		SettingsUtils.class).key("breadcrumb.title"));

	public static HtmlLinkState getBreadcrumb()
	{
		HtmlLinkState link = new HtmlLinkState(SETTINGS_BOOKMARK);
		link.setLabel(BREADCRUMB_LABEL);
		link.setTitle(BREADCRUMB_TITLE);
		return link;
	}

	private SettingsUtils()
	{
		throw new Error();
	}
}
