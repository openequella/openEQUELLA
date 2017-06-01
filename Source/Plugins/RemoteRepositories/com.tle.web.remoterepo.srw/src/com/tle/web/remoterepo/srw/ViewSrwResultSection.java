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

package com.tle.web.remoterepo.srw;

import java.util.List;

import javax.inject.Inject;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.entity.FederatedSearch;
import com.tle.core.fedsearch.GenericRecord;
import com.tle.core.guice.Bind;
import com.tle.core.remoterepo.srw.service.SrwService;
import com.tle.web.remoterepo.section.RemoteRepoViewResultSection;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.selection.section.CourseListVetoSection;

/**
 * @author agibb
 * @author aholland
 */
@SuppressWarnings("nls")
@Bind
public class ViewSrwResultSection
	extends
		RemoteRepoViewResultSection<ViewSrwResultSection.ViewSrwResultModel, SrwListEntry, GenericRecord>
	implements
		CourseListVetoSection
{
	private static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(ViewSrwResultSection.class);

	@Inject
	private SrwService srwService;

	@TreeLookup
	private SrwQuerySection querySection;

	@Override
	protected void getContents(SectionInfo info, ViewSrwResultModel model, List<SectionRenderable> contents)
	{
		GenericRecord result = model.getResult();
		addField(contents, "view.description", result.getDescription());
		addField(contents, "view.author", result.getAuthors());
		addField(contents, "view.isbn", result.getIsbn());
		addField(contents, "view.issn", result.getIssn());
		addField(contents, "view.lccn", result.getLccn());
		addField(contents, "view.physicaldescription", result.getPhysicalDescription());
	}

	@Override
	protected PropBagEx getImportXml(SectionInfo info, FederatedSearch search)
	{
		ViewSrwResultModel model = getModel(info);
		PropBagEx resultXml = srwService.getRecord(search, querySection.getQueryField().getValue(info),
			model.getResultIndex()).getXml();
		return resultXml;
	}

	@Override
	protected Label getTitle(SectionInfo info, ViewSrwResultModel model)
	{
		return new TextLabel(model.getResult().getTitle());
	}

	@Override
	protected void setupModel(SectionInfo info, ViewSrwResultModel model, FederatedSearch search)
	{
		GenericRecord rec = srwService.getRecord(search, querySection.getQueryField().getValue(info),
			model.getResultIndex());
		model.setResult(rec);
	}

	@Override
	public Class<ViewSrwResultModel> getModelClass()
	{
		return ViewSrwResultModel.class;
	}

	@Override
	protected void clearResult(SectionInfo info)
	{
		getModel(info).setView(false);
	}

	@Override
	public boolean isShowing(SectionInfo info)
	{
		return getModel(info).isView();
	}

	@Override
	public Bookmark getViewHandler(SectionInfo info, SrwListEntry listItem)
	{
		return new BookmarkAndModify(info, events.getNamedModifier("viewResult", listItem.getResult().getIndex()));
	}

	// FIXME: No, we don't really want to do this. it would be much better to
	// get a result via a UUID rather than re-doing the search with an index
	@EventHandlerMethod
	public void viewResult(SectionInfo info, int resultIndex)
	{
		final ViewSrwResultModel model = getModel(info);
		model.setView(true);
		model.setResultIndex(resultIndex);
	}

	@Override
	protected String getKeyPrefix()
	{
		return resources.pluginId() + ".";
	}

	public static class ViewSrwResultModel extends RemoteRepoViewResultSection.RemoteRepoViewResultModel<GenericRecord>
	{
		@Bookmarked(name = "v")
		private boolean view;
		@Bookmarked(name = "r")
		private int resultIndex;

		public boolean isView()
		{
			return view;
		}

		public void setView(boolean view)
		{
			this.view = view;
		}

		public int getResultIndex()
		{
			return resultIndex;
		}

		public void setResultIndex(int resultIndex)
		{
			this.resultIndex = resultIndex;
		}
	}
}
