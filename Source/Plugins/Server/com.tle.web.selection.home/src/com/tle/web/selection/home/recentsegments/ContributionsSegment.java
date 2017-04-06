package com.tle.web.selection.home.recentsegments;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemStatus;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.search.DefaultSearch;
import com.tle.common.searching.Search.SortType;
import com.tle.common.searching.SearchResults;
import com.tle.core.services.item.FreeTextService;
import com.tle.core.user.CurrentUser;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.home.model.RecentSelectionSegmentModel.RecentSelection;
import com.tle.web.viewurl.ViewItemUrlFactory;

/**
 * @author aholland
 */
public class ContributionsSegment extends AbstractRecentSegment
{
	@Inject
	private FreeTextService searchService;
	@Inject
	private ViewItemUrlFactory urlFactory;
	@Inject
	private SelectionService selectionService;

	@Override
	protected List<RecentSelection> getSelections(SectionInfo info, SelectionSession session, int maximum)
	{
		List<RecentSelection> selections = new ArrayList<RecentSelection>();
		DefaultSearch search = new DefaultSearch();
		search.setOwner(CurrentUser.getUserID());
		search.setPrivilege(selectionService.getSearchPrivilege(info));
		if( !session.isAllCollections() )
		{
			Set<String> collectionUuids = session.getCollectionUuids();
			if( !collectionUuids.isEmpty() )
			{
				search.setCollectionUuids(collectionUuids);
			}
			else
			{
				// there is nothing available...
				return selections;
			}
		}
		Set<String> mimeTypes = session.getMimeTypes();
		if( mimeTypes != null && !mimeTypes.isEmpty() )
		{
			search.setMimeTypes(mimeTypes);
		}

		search.setNotItemStatuses(ItemStatus.PERSONAL);
		search.setSortType(SortType.DATEMODIFIED);

		SearchResults<Item> results = searchService.search(search, 0, maximum);
		List<Item> items = results.getResults();
		for( Item item : items )
		{
			HtmlLinkState state = new HtmlLinkState(urlFactory.createItemUrl(info, item.getItemId()));
			selections.add(new RecentSelection(CurrentLocale.get(item.getName(), item.getIdString()), state));
		}
		return selections;
	}

	@Override
	public String getTitle(SectionInfo info, SelectionSession session)
	{
		return CurrentLocale.get("com.tle.web.selection.home.recently.contributed"); //$NON-NLS-1$
	}
}
