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

package com.tle.web.remoterepo.z3950;

import java.util.List;

import javax.inject.Inject;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.entity.FederatedSearch;
import com.tle.common.Check;
import com.tle.core.fedsearch.GenericRecord;
import com.tle.core.guice.Bind;
import com.tle.core.remoterepo.parser.mods.ModsRecord;
import com.tle.core.remoterepo.z3950.Z3950SearchResult;
import com.tle.core.remoterepo.z3950.service.Z3950Service;
import com.tle.web.remoterepo.section.RemoteRepoViewResultSection;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.selection.section.CourseListVetoSection;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
@Bind
public class ViewZ3950ResultSection
	extends
		RemoteRepoViewResultSection<ViewZ3950ResultSection.ViewZ3950ResultModel, Z3950ListEntry, GenericRecord>
	implements
		CourseListVetoSection
{
	private static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(ViewZ3950ResultSection.class);

	@Inject
	private Z3950Service z3950Service;

	@Override
	protected void setupModel(SectionInfo info, ViewZ3950ResultModel model, FederatedSearch search)
	{
		model.setResult(resolveRecord(info, search, false));
	}

	@Override
	protected Label getTitle(SectionInfo info, ViewZ3950ResultModel model)
	{
		return new TextLabel(model.getResult().getTitle());
	}

	@Override
	protected void getContents(SectionInfo info, ViewZ3950ResultModel model, List<SectionRenderable> contents)
	{
		final GenericRecord result = model.getResult();
		addField(contents, "view.description", result.getDescription());
		if( !Check.isEmpty(result.getAuthors()) )
		{
			addField(contents, "view.author", result.getAuthors().toArray());
		}
		addField(contents, "view.isbn", result.getIsbn());
		addField(contents, "view.issn", result.getIssn());
		addField(contents, "view.lccn", result.getLccn());
		addField(contents, "view.physicaldescription", result.getPhysicalDescription());

		if( result instanceof ModsRecord )
		{
			final ModsRecord mods = (ModsRecord) result;
			addField(contents, "view.notes", mods.getNotes().toArray());
			addField(contents, "view.typeofresource", mods.getTypeOfResource());
		}
	}

	@Override
	protected PropBagEx getImportXml(SectionInfo info, FederatedSearch search)
	{
		return resolveRecord(info, search, true).getXml();
	}

	@Override
	public Class<ViewZ3950ResultModel> getModelClass()
	{
		return ViewZ3950ResultModel.class;
	}

	@Override
	protected void clearResult(SectionInfo info)
	{
		getModel(info).setShowResults(false);
	}

	@Override
	public boolean isShowing(SectionInfo info)
	{
		final ViewZ3950ResultModel model = getModel(info);
		return model.isShowResults();
	}

	private GenericRecord resolveRecord(SectionInfo info, FederatedSearch z3950Search, boolean useImportSchema)
	{
		final ViewZ3950ResultModel model = getModel(info);
		final Z3950SearchEvent searchEvent = new Z3950SearchEvent(getRootRemoteRepoSection(), z3950Search);
		info.processEvent(searchEvent);
		return z3950Service.getRecord(searchEvent.getSearch(), searchEvent.getQuery(), model.getRecordIndex(),
			searchEvent.getAdvancedOptions(), useImportSchema);
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "z";
	}

	@Override
	public Bookmark getViewHandler(SectionInfo info, Z3950ListEntry listItem)
	{
		final Z3950SearchResult result = listItem.getResult();
		return new BookmarkAndModify(info, events.getNamedModifier("viewResult", result.getIndex())); //$NON-NLS-1$
	}

	@EventHandlerMethod
	public void viewResult(SectionInfo info, int index)
	{
		final ViewZ3950ResultModel model = getModel(info);
		model.setRecordIndex(index);
		model.setShowResults(true);
	}

	@Override
	protected String getKeyPrefix()
	{
		return resources.pluginId() + ".";
	}

	public static class ViewZ3950ResultModel
		extends
			RemoteRepoViewResultSection.RemoteRepoViewResultModel<GenericRecord>
	{
		@Bookmarked(name = "r")
		private int recordIndex;
		@Bookmarked(name = "s")
		private boolean showResults;

		public boolean isShowResults()
		{
			return showResults;
		}

		public void setShowResults(boolean showResults)
		{
			this.showResults = showResults;
		}

		public int getRecordIndex()
		{
			return recordIndex;
		}

		public void setRecordIndex(int recordIndex)
		{
			this.recordIndex = recordIndex;
		}
	}
}
