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

package com.tle.web.remoterepo.merlot;

import javax.inject.Inject;

import com.tle.web.remoterepo.section.AbstractRootRemoteRepoSection;
import com.tle.web.remoterepo.section.RemoteRepoViewResultSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.layout.ContentLayout;

@SuppressWarnings("nls")
public class MerlotRootRemoteRepoSection extends AbstractRootRemoteRepoSection
{
	protected static final String CONTEXT_KEY = "merlotRepoContext";

	@Inject
	private ViewMerlotResultSection viewResult;

	@Override
	protected ContentLayout getDefaultLayout(SectionInfo info)
	{
		return ContentLayout.ONE_COLUMN;
	}

	@Override
	protected String getSessionKey()
	{
		return CONTEXT_KEY;
	}

	@Override
	protected RemoteRepoViewResultSection<?, ?, ?> getViewSection()
	{
		return viewResult;
	}
}
