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

package com.tle.web.favourites;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tle.web.favourites.searches.SearchFavouritesResultsSection;
import com.tle.web.search.base.ContextableSearchSection;
import com.tle.web.sections.MutableSectionInfo;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.utils.VoidKeyOption;
import com.tle.web.sections.events.ParametersEvent;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.generic.ReloadHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.event.ValueSetListener;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;

@SuppressWarnings("nls")
public abstract class AbstractRootFavouritesSection extends ContextableSearchSection<ContextableSearchSection.Model>
	implements
		ValueSetListener<Set<String>>
{
	private static final String CONTEXT_KEY = "favouritesContext";

	private static final String SEARCHES_TYPE = "searches";
	private static final String ITEMS_TYPE = "items";

	@PlugKey("favourites.title")
	private static Label LABEL_TITLE;
	@PlugKey(SEARCHES_TYPE)
	private static String KEY_SEARCHES;
	@PlugKey(ITEMS_TYPE)
	private static String KEY_ITEMS;

	@Component
	private SingleSelectionList<Void> favouriteType;

	protected abstract SectionTree getSearchTree();

	protected abstract SectionTree getItemTree();

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		favouriteType.setListModel(new SimpleHtmlListModel<Void>(new VoidKeyOption(KEY_ITEMS, ITEMS_TYPE),
			new VoidKeyOption(KEY_SEARCHES, SEARCHES_TYPE)));
		favouriteType.addChangeEventHandler(new ReloadHandler());
		favouriteType.setAlwaysSelect(true);
		favouriteType.setValueSetListener(this);
	}

	@Override
	protected String getSessionKey()
	{
		return CONTEXT_KEY;
	}

	@Override
	public Label getTitle(SectionInfo info)
	{
		return LABEL_TITLE;
	}

	public Label getHeaderTitle()
	{
		return LABEL_TITLE;
	}

	@Override
	protected boolean hasContextBeenSpecified(SectionInfo info)
	{
		return getModel(info).isUpdateContext();
	}

	@Override
	public void valueSet(SectionInfo info, Set<String> value)
	{
		MutableSectionInfo minfo = info.getAttributeForClass(MutableSectionInfo.class);
		SectionTree tree = getCurrentTree(info);
		minfo.addTreeToBottom(tree, true);
		Map<String, String[]> context = getModel(info).getContext();
		if( context != null )
		{
			info.processEvent(new ParametersEvent(context, false), tree);
			if( favouriteType.getSelectedValueAsString(info).equals(SEARCHES_TYPE) )
			{
				SearchFavouritesResultsSection searchFavouritesResultsSection = info
					.lookupSection(SearchFavouritesResultsSection.class);
				if( searchFavouritesResultsSection != null )
				{
					searchFavouritesResultsSection.startSearch(info);
				}
			}
			else
			{
				FavouritesResultsSection favouritesResultsSection = info.lookupSection(FavouritesResultsSection.class);
				if( favouritesResultsSection != null )
				{

					favouritesResultsSection.startSearch(info);
				}
			}
		}
	}

	@Override
	protected List<SectionId> getChildIds(RenderContext info)
	{
		SectionTree tree = getCurrentTree(info);
		return tree.getChildIds(tree.getRootId());
	}

	private SectionTree getCurrentTree(SectionInfo info)
	{
		String list = favouriteType.getSelectedValueAsString(info);
		return list.equals(SEARCHES_TYPE) ? getSearchTree() : getItemTree();
	}

	@Override
	protected SectionRenderable getBodyHeader(RenderContext info)
	{
		return viewFactory.createResult("favourites.ftl", this);
	}

	public SingleSelectionList<Void> getFavouriteType()
	{
		return favouriteType;
	}
}
