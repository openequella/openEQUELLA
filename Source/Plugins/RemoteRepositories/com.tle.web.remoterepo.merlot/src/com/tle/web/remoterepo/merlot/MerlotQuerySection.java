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

import java.util.Collection;

import javax.inject.Inject;

import com.tle.beans.entity.FederatedSearch;
import com.tle.beans.search.MerlotSettings;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.i18n.CurrentLocale;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.remoterepo.section.RemoteRepoQuerySection;
import com.tle.web.remoterepo.section.RemoteRepoResultsSection;
import com.tle.web.remoterepo.service.RemoteRepoWebService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.JSValidator;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public class MerlotQuerySection extends RemoteRepoQuerySection<MerlotRemoteRepoSearchEvent>
{
	@Inject
	private MerlotWebService merlotWebService;
	@Inject
	private RemoteRepoWebService repoWebService;

	@TreeLookup
	private RemoteRepoResultsSection<?, ?, ?> searchResults;

	@Component(name = "cm")
	private SingleSelectionList<NameValue> communities;
	@Component(name = "m")
	private SingleSelectionList<NameValue> materialTypes;
	@Component(name = "c")
	private SingleSelectionList<NameValue> categories;
	@Component(name = "s")
	private SingleSelectionList<NameValue> subcategories;

	@ViewFactory
	private FreemarkerFactory view;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		final FederatedSearch search = repoWebService.getRemoteRepository(context);
		final MerlotSettings settings = new MerlotSettings();
		settings.load(search);
		if( settings.isAdvancedApi() )
		{
			// try to get the options. we may not be able to talk to MERLOT and
			// we want a proper error page, not an embedded freemarker stack
			// trace.
			communities.getListModel().getOptions(context);

			MerlotModel model = (MerlotModel) getModel(context);
			model.setTitle(CurrentLocale.get(search.getName()));
			model.setCategorySelected(!Check.isEmpty(categories.getSelectedValueAsString(context)));
			return view.createResult("advancedquery.ftl", this);
		}
		return super.renderHtml(context);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		categories.setListModel(new MerlotFilterListModel(ROOT_CATEGORIES));
		subcategories.setListModel(new MerlotFilterListModel(SUB_CATEGORIES));
		materialTypes.setListModel(new MerlotFilterListModel(MATERIAL_TYPES));
		communities.setListModel(new MerlotFilterListModel(COMMUNITIES));
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);

		final JSHandler reSearch = searchResults.getRestartSearchHandler(tree);
		subcategories.addEventStatements(JSHandler.EVENT_CHANGE, reSearch);
		materialTypes.addEventStatements(JSHandler.EVENT_CHANGE, reSearch);
		communities.addEventStatements(JSHandler.EVENT_CHANGE, reSearch);

		// Also need to reload the subcategories control when this changes.
		categories.addEventStatements(JSHandler.EVENT_CHANGE,
			new OverrideHandler(searchResults.getResultsUpdater(tree, null, "subcategories")));
	}

	@Override
	protected JSValidator createValidator()
	{
		return null;
	}

	@Override
	public void prepareSearch(SectionInfo info, MerlotRemoteRepoSearchEvent event) throws Exception
	{
		event.filterByTextQuery(getParsedQuery(info), true);
		event.setMaterialType(materialTypes.getSelectedValueAsString(info));
		event.setCommunity(communities.getSelectedValueAsString(info));

		String cat = categories.getSelectedValueAsString(info);
		if( !Check.isEmpty(cat) )
		{
			String subcat = subcategories.getSelectedValueAsString(info);
			event.setCategory(Check.isEmpty(subcat) ? cat : subcat);
		}
	}

	public SingleSelectionList<NameValue> getCommunities()
	{
		return communities;
	}

	public SingleSelectionList<NameValue> getMaterialTypes()
	{
		return materialTypes;
	}

	public SingleSelectionList<NameValue> getCategories()
	{
		return categories;
	}

	public SingleSelectionList<NameValue> getSubcategories()
	{
		return subcategories;
	}

	private final MerlotFilterType COMMUNITIES = new MerlotFilterType()
	{
		@Override
		public Collection<NameValue> getValues(SectionInfo info)
		{
			return merlotWebService.getCommunities(info);
		}
	};

	private final MerlotFilterType MATERIAL_TYPES = new MerlotFilterType()
	{
		@Override
		public Collection<NameValue> getValues(SectionInfo info)
		{
			return merlotWebService.getMaterialTypes(info);
		}
	};

	private final MerlotFilterType ROOT_CATEGORIES = new MerlotFilterType()
	{
		@Override
		public Collection<NameValue> getValues(SectionInfo info)
		{
			return merlotWebService.getCategories(info, null);
		}
	};

	private final MerlotFilterType SUB_CATEGORIES = new MerlotFilterType()
	{
		@Override
		public Collection<NameValue> getValues(SectionInfo info)
		{
			return merlotWebService.getCategories(info, categories.getSelectedValueAsString(info));
		}
	};

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new MerlotModel();
	}

	public static class MerlotModel extends RemoteRepoQueryModel
	{
		private boolean categorySelected;

		public boolean isCategorySelected()
		{
			return categorySelected;
		}

		public void setCategorySelected(boolean categorySelected)
		{
			this.categorySelected = categorySelected;
		}
	}
}
