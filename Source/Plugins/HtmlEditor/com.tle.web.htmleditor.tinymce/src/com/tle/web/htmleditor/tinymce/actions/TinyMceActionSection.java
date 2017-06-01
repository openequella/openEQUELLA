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

package com.tle.web.htmleditor.tinymce.actions;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.DynaCollection;
import com.tle.beans.entity.PowerSearch;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.web.htmleditor.tinymce.TinyMceAddOn;
import com.tle.web.htmleditor.tinymce.service.TinyMceService;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;

/*
 * @author aholland
 */
@NonNullByDefault
public class TinyMceActionSection extends AbstractPrototypeSection<TinyMceActionSection.TinyMceActionModel>
	implements
		HtmlRenderer
{
	@Inject
	private TinyMceService tinyMceService;

	private final PluginResourceHelper resources = ResourcesService.getResourceHelper(TinyMceActionSection.class);

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		List<TinyMceAddOn> addOns = tinyMceService.getAddOns();
		for( TinyMceAddOn tinyMceAddOn : addOns )
		{
			tinyMceAddOn.register(tree, id);
		}
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		TinyMceActionModel model = getModel(context);
		String action = model.getAction();

		for( TinyMceAddOn addOn : tinyMceService.getAddOns() )
		{
			if( addOn.applies(action) )
			{
				//@formatter:off
				return addOn
					.execute(
						context,
						action,
						model.getSessionId(),
						model.getPageId(),
						resources.url(""), //$NON-NLS-1$
						model.isRestrictedCollections(), model.getCollectionUuids(),
						model.isRestrictedDynacolls(), model.getDynaCollectionUuids(),
						model.isRestrictedSearches(), model.getSearchUuids(),
						model.isRestrictedContributables(), model.getContributableUuids());
				//@formatter:on
			}
		}

		return null;
	}

	public void setSearchableUuids(SectionInfo info, boolean restrictedCollections, boolean restrictedDynacolls,
		boolean restrictedSearches, boolean restrictedContributables,
		@Nullable Map<Class<?>, Set<String>> searchableUuids, Set<String> contributableUuids)
	{
		TinyMceActionModel model = getModel(info);
		model.setRestrictedCollections(restrictedCollections);
		model.setRestrictedDynacolls(restrictedDynacolls);
		model.setRestrictedSearches(restrictedSearches);
		model.setRestrictedContributables(restrictedContributables);
		if( searchableUuids != null )
		{
			model.setCollectionUuids(searchableUuids.get(ItemDefinition.class));
			model.setDynaCollectionUuids(searchableUuids.get(DynaCollection.class));
			model.setSearchUuids(searchableUuids.get(PowerSearch.class));
			model.setContributableUuids(contributableUuids);
		}
	}

	@Override
	public String getDefaultPropertyName()
	{
		return ""; //$NON-NLS-1$
	}

	public void setSessionId(SectionInfo info, String previewId)
	{
		getModel(info).setSessionId(previewId);
	}

	public void setPageId(SectionInfo info, String stagingId)
	{
		getModel(info).setPageId(stagingId);
	}

	@Override
	public Class<TinyMceActionModel> getModelClass()
	{
		return TinyMceActionModel.class;
	}

	public static class TinyMceActionModel
	{
		@Bookmarked
		protected String action;
		@Bookmarked
		protected String sessionId;
		@Bookmarked
		protected String pageId;
		@Bookmarked
		protected boolean restrictedCollections;
		@Bookmarked
		protected Set<String> collectionUuids;
		@Bookmarked
		protected boolean restrictedDynacolls;
		@Bookmarked
		protected Set<String> dynaCollectionUuids;
		@Bookmarked
		protected boolean restrictedSearches;
		@Bookmarked
		protected Set<String> searchUuids;
		@Bookmarked
		protected boolean restrictedContributables;
		@Bookmarked
		protected Set<String> contributableUuids;

		public String getAction()
		{
			return action;
		}

		public void setAction(String action)
		{
			this.action = action;
		}

		public String getSessionId()
		{
			return sessionId;
		}

		public void setSessionId(String sessionId)
		{
			this.sessionId = sessionId;
		}

		public String getPageId()
		{
			return pageId;
		}

		public void setPageId(String pageId)
		{
			this.pageId = pageId;
		}

		public boolean isRestrictedCollections()
		{
			return restrictedCollections;
		}

		public void setRestrictedCollections(boolean restrictedCollections)
		{
			this.restrictedCollections = restrictedCollections;
		}

		public Set<String> getCollectionUuids()
		{
			return collectionUuids;
		}

		public void setCollectionUuids(Set<String> collectionUuids)
		{
			this.collectionUuids = collectionUuids;
		}

		public boolean isRestrictedDynacolls()
		{
			return restrictedDynacolls;
		}

		public void setRestrictedDynacolls(boolean restrictedDynacolls)
		{
			this.restrictedDynacolls = restrictedDynacolls;
		}

		public Set<String> getDynaCollectionUuids()
		{
			return dynaCollectionUuids;
		}

		public void setDynaCollectionUuids(Set<String> dynaCollectionUuids)
		{
			this.dynaCollectionUuids = dynaCollectionUuids;
		}

		public boolean isRestrictedSearches()
		{
			return restrictedSearches;
		}

		public void setRestrictedSearches(boolean restrictedSearches)
		{
			this.restrictedSearches = restrictedSearches;
		}

		public Set<String> getSearchUuids()
		{
			return searchUuids;
		}

		public void setSearchUuids(Set<String> searchUuids)
		{
			this.searchUuids = searchUuids;
		}

		public boolean isRestrictedContributables()
		{
			return restrictedContributables;
		}

		public void setRestrictedContributables(boolean restrictedContributables)
		{
			this.restrictedContributables = restrictedContributables;
		}

		public Set<String> getContributableUuids()
		{
			return contributableUuids;
		}

		public void setContributableUuids(Set<String> contributableUuids)
		{
			this.contributableUuids = contributableUuids;
		}
	}
}
