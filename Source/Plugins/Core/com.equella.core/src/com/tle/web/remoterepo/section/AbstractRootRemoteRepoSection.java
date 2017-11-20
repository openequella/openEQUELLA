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

package com.tle.web.remoterepo.section;

import javax.inject.Inject;

import com.tle.beans.entity.FederatedSearch;
import com.tle.core.i18n.BundleCache;
import com.tle.web.navigation.BreadcrumbService;
import com.tle.web.remoterepo.RemoteRepoSection;
import com.tle.web.remoterepo.service.RemoteRepoWebService;
import com.tle.web.search.base.ContextableSearchSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.layout.ContentLayout;
import com.tle.web.sections.equella.search.AbstractQuerySection;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.selection.section.CourseListVetoSection;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

/**
 * @author aholland
 */
@TreeIndexed
public abstract class AbstractRootRemoteRepoSection
	extends
		ContextableSearchSection<AbstractRootRemoteRepoSection.Model>
	implements
		RemoteRepoSection,
		CourseListVetoSection
{
	@Inject
	private RemoteRepoWebService repoWebService;
	@Inject
	private BundleCache bundleCache;
	@TreeLookup(mandatory = false)
	private AbstractQuerySection<?, ?> querySection;

	@Inject
	private BreadcrumbService breadcrumbService;
	private RemoteRepoViewResultSection<?, ?, ?> resultViewer;

	@Override
	protected abstract ContentLayout getDefaultLayout(SectionInfo info);

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		resultViewer = getViewSection();
		if( resultViewer != null )
		{
			tree.registerInnerSection(resultViewer, id);
		}
	}

	protected abstract RemoteRepoViewResultSection<?, ?, ?> getViewSection();

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( resultViewer != null && resultViewer.isShowing(context) )
		{
			ContentLayout.setLayout(context, ContentLayout.ONE_COLUMN);
			setModalSection(context, resultViewer);
		}
		return super.renderHtml(context);
	}

	@Override
	protected void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		super.addBreadcrumbsAndTitle(info, decorations, crumbs);
		crumbs.add(breadcrumbService.getContributeCrumb(info));
		if( resultViewer != null && resultViewer.isShowing(info) )
		{
			resultViewer.addCrumbs(info, crumbs);
		}
	}

	@Override
	public String getSearchUuid(SectionInfo info)
	{
		return getModel(info).getSearchUuid();
	}

	@Override
	protected boolean hasContextBeenSpecified(SectionInfo info)
	{
		if( querySection == null )
		{
			return false;
		}
		return super.hasContextBeenSpecified(info);
	}

	@Override
	public void setSearchUuid(SectionInfo info, String searchUuid)
	{
		getModel(info).setSearchUuid(searchUuid);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Model();
	}

	public static class Model extends ContextableSearchSection.Model
	{
		@Bookmarked(name = "repository", contexts = BookmarkEvent.CONTEXT_BROWSERURL, supported = true)
		private String searchUuid;

		public String getSearchUuid()
		{
			return searchUuid;
		}

		public void setSearchUuid(String searchUuid)
		{
			this.searchUuid = searchUuid;
		}
	}

	@Override
	public Label getTitle(SectionInfo info)
	{
		final FederatedSearch search = repoWebService.getRemoteRepository(info);
		if( search != null )
		{
			return new BundleLabel(search.getName(), bundleCache);
		}
		return null;
	}
}
