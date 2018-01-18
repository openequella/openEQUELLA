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

package com.tle.web.remoterepo.sru;

import java.util.List;

import javax.inject.Inject;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.entity.FederatedSearch;
import com.tle.core.fedsearch.GenericRecord;
import com.tle.core.guice.Bind;
import com.tle.core.remoterepo.sru.service.SruService;
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
 * @author larry
 */
@Bind
public class ViewSruResultSection
	extends
		RemoteRepoViewResultSection<ViewSruResultSection.ViewSruResultModel, SruListEntry, GenericRecord>
	implements
		CourseListVetoSection
{
	private static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(ViewSruResultSection.class);

	@Inject
	private SruService sruService;

	@TreeLookup
	private SruQuerySection querySection;

	@Override
	@SuppressWarnings("nls")
	protected void getContents(SectionInfo info, ViewSruResultModel model, List<SectionRenderable> contents)
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
		ViewSruResultModel model = getModel(info);
		PropBagEx resultXml = sruService.getRecord(search, querySection.getQueryField().getValue(info),
			model.getResultIndex()).getXml();
		return resultXml;
	}

	@Override
	protected Label getTitle(SectionInfo info, ViewSruResultModel model)
	{
		return new TextLabel(model.getResult().getTitle());
	}

	@Override
	protected void setupModel(SectionInfo info, ViewSruResultModel model, FederatedSearch search)
	{
		GenericRecord rec = sruService.getRecord(search, querySection.getQueryField().getValue(info),
			model.getResultIndex());
		model.setResult(rec);
	}

	@Override
	public Class<ViewSruResultModel> getModelClass()
	{
		return ViewSruResultModel.class;
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
	public Bookmark getViewHandler(SectionInfo info, SruListEntry listItem)
	{
		return new BookmarkAndModify(info, events.getNamedModifier("viewResult", listItem.getResult().getIndex())); //$NON-NLS-1$
	}

	// FIXME: see comments to ViewSrwResultSection.viewResult
	@EventHandlerMethod
	public void viewResult(SectionInfo info, int resultIndex)
	{
		final ViewSruResultModel model = getModel(info);
		model.setView(true);
		model.setResultIndex(resultIndex);
	}

	@Override
	protected String getKeyPrefix()
	{
		return resources.pluginId() + "."; //$NON-NLS-1$
	}

	public static class ViewSruResultModel extends RemoteRepoViewResultSection.RemoteRepoViewResultModel<GenericRecord>
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
