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

package com.tle.web.remoterepo.equella;

import javax.inject.Inject;

import com.tle.core.guice.Bind;
import com.tle.web.remoterepo.section.AbstractRootRemoteRepoSection;
import com.tle.web.remoterepo.section.RemoteRepoViewResultSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.layout.ContentLayout;
import com.tle.web.sections.events.RenderEventContext;

@SuppressWarnings("nls")
@Bind
public class EquellaRootRemoteRepoSection extends AbstractRootRemoteRepoSection
{
	protected static final String CONTEXT_KEY = "equellaRepoContext";

	@Inject
	private EquellaRepoDownloadProgressSection progressSection;

	@Override
	protected ContentLayout getDefaultLayout(SectionInfo info)
	{
		return ContentLayout.ONE_COLUMN;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.registerInnerSection(progressSection, id);
	}

	@Override
	protected String getContentBodyClasses()
	{
		return "repo-onecol";
	}

	@Override
	protected String getSessionKey()
	{
		return CONTEXT_KEY;
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( progressSection.isShowing(context) )
		{
			setModalSection(context, progressSection);
		}
		return super.renderHtml(context);
	}

	@Override
	protected RemoteRepoViewResultSection<?, ?, ?> getViewSection()
	{
		return null;
	}

}
