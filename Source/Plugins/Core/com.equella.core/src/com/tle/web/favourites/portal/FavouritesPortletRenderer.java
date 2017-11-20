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

package com.tle.web.favourites.portal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import com.dytech.edge.queries.FreeTextQuery;
import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.Bookmark;
import com.tle.beans.item.Item;
import com.tle.common.searching.SortField;
import com.tle.core.favourites.bean.FavouriteSearch;
import com.tle.core.favourites.service.BookmarkService;
import com.tle.core.favourites.service.FavouriteSearchService;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleCache;
import com.tle.core.security.TLEAclManager;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.web.favourites.FavouritesResultsSection.FavouritesSearch;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.portal.renderer.PortletContentRenderer;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourceHelper;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.viewurl.ViewItemUrlFactory;

@NonNullByDefault
@SuppressWarnings("nls")
@Bind
public class FavouritesPortletRenderer extends PortletContentRenderer<FavouritesPortletRenderer.Model>
{
	private static int MAX_ENTRIES = 5;

	@EventFactory
	private EventGenerator events;
	@ResourceHelper
	private PluginResourceHelper RESOURCES;

	@Inject
	private BookmarkService favItemService;
	@Inject
	private FavouriteSearchService favSearchService;
	@Inject
	private FreeTextService freeTextService;
	@Inject
	private SelectionService selectionService;
	@Inject
	private TLEAclManager aclService;

	@Inject
	private BundleCache bundleCache;
	@Inject
	private ViewItemUrlFactory itemUrls;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Component
	@PlugKey("portal.showall")
	private Button showAll;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		showAll.setClickHandler(events.getNamedHandler("showFavourites"));
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		List<FavouriteRow> favs = new ArrayList<FavouriteRow>();

		final SelectionSession session = selectionService.getCurrentSession(context);
		final String priv = (session != null && session.isUseDownloadPrivilege() ? "DOWNLOAD_ITEM" : "DISCOVER_ITEM");
		final Collection<Item> bookmarkItems = aclService.filterNonGrantedObjects(Collections.singleton(priv),
			searchFavItems());

		for( Bookmark b : favItemService.getBookmarksForItems(bookmarkItems).values() )
		{
			favs.add(new FavouriteRow(b, context));
		}

		for( FavouriteSearch fs : favSearchService.getSearchesForOwner(CurrentUser.getUserID(), MAX_ENTRIES) )
		{
			favs.add(new FavouriteRow(fs));
		}

		// Sort by date
		Collections.sort(favs, new Comparator<FavouriteRow>()
		{
			@Override
			public int compare(FavouriteRow f1, FavouriteRow f2)
			{
				return -f1.getDateModified().compareTo(f2.getDateModified());
			}
		});

		if( favs.size() > MAX_ENTRIES )
		{
			favs = favs.subList(0, MAX_ENTRIES);
		}
		getModel(context).setFavourites(favs);

		return viewFactory.createResult("portal/favouritesportal.ftl", this);
	}

	@EventHandlerMethod
	public void showFavourites(SectionInfo info)
	{
		info.forward(info.createForward("/access/favourites.do"));
	}

	private List<Item> searchFavItems()
	{
		FavouritesSearch search = new FavouritesSearch();
		search.setSortFields(new SortField(FreeTextQuery.FIELD_BOOKMARK_DATE + CurrentUser.getUserID(), true));
		return freeTextService.search(search, 0, MAX_ENTRIES).getResults();
	}

	@Override
	public boolean canView(SectionInfo info)
	{
		return true;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "fav";
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Model();
	}

	@EventHandlerMethod
	public void execSearch(SectionInfo info, long searchId)
	{
		favSearchService.executeSearch(info, searchId);
	}

	public static class Model
	{
		private List<FavouriteRow> favourites = new ArrayList<FavouriteRow>();

		public List<FavouriteRow> getFavourites()
		{
			return favourites;
		}

		public void setFavourites(List<FavouriteRow> favourites)
		{
			this.favourites = favourites;
		}
	}

	public class FavouriteRow
	{
		private final Label label;
		private final HtmlComponentState link;
		private final Date dateModified;

		public FavouriteRow(FavouriteSearch search)
		{
			KeyLabel linkLabel = new KeyLabel(RESOURCES.key("portal.searchresult"), search.getName());
			linkLabel.setHtml(false);
			this.label = linkLabel;
			this.link = new HtmlLinkState(events.getNamedHandler("execSearch", search.getId()));
			this.dateModified = search.getDateModified();
		}

		public FavouriteRow(Bookmark bookmark, SectionInfo info)
		{
			this.label = new BundleLabel(bookmark.getItem().getName(), bookmark.getItem().getUuid(), bundleCache);
			this.link = new HtmlLinkState(itemUrls.createItemUrl(info, bookmark.getItem().getItemId()));
			this.dateModified = bookmark.getDateModified();
		}

		public Label getLabel()
		{
			return label;
		}

		public HtmlComponentState getLink()
		{
			return link;
		}

		public Date getDateModified()
		{
			return dateModified;
		}
	}

	public Button getShowAll()
	{
		return showAll;
	}
}