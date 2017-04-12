package com.tle.web.payment.storefront.section;

import java.util.List;

import javax.inject.Inject;

import com.tle.core.guice.Bind;
import com.tle.core.payment.StoreFrontIndexFields;
import com.tle.core.user.CurrentUser;
import com.tle.web.myresources.MyResourcesSearch;
import com.tle.web.myresources.MyResourcesSubSearch;
import com.tle.web.myresources.MyResourcesSubSubSearch;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.generic.AbstractPrototypeSection;

@SuppressWarnings("nls")
@Bind
public class PurchasedSubSearch extends AbstractPrototypeSection<PurchasedSubSearch.Model>
	implements
		MyResourcesSubSearch
{
	@TreeLookup
	private SearchResultsActionsSection actionsSection;

	@Inject
	private PurchasedItemList itemList;

	private static PluginResourceHelper helper = ResourcesService.getResourceHelper(PurchasedSubSearch.class);

	@Override
	public int getOrder()
	{
		return 1200;
	}

	@Override
	public MyResourcesSearch createDefaultSearch(SectionInfo info)
	{
		MyResourcesSearch search = new MyResourcesSearch()
		{
			@Override
			public String getOwner()
			{
				return null;
			}
		};
		search.addMust(StoreFrontIndexFields.FIELD_CHECKED_OUT_BY, CurrentUser.getUserID());
		return search;
	}

	@Override
	public PurchasedItemList getCustomItemList()
	{
		return itemList;

	}

	@Override
	public List<MyResourcesSubSubSearch> getSubSearches()
	{
		return null;
	}

	@Override
	public void setupFilters(SectionInfo info)
	{
		getModel(info).setEnabled(true);
		actionsSection.disableSaveAndShare(info);
	}

	@Override
	public void register(SectionTree tree, String parentId)
	{
		tree.registerInnerSection(this, parentId);
		tree.registerInnerSection(itemList, parentId);

	}

	@Override
	public String getNameKey()
	{
		return helper.key("purchased.searchname");
	}

	@Override
	public String getValue()
	{
		return "purchased";
	}

	@Override
	public boolean isShownOnPortal()
	{
		return false; // mebbe true?
	}

	@Override
	public boolean canView()
	{
		return true;
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Model();
	}

	public static class Model
	{
		private boolean enabled;

		public boolean isEnabled()
		{
			return enabled;
		}

		public void setEnabled(boolean enabled)
		{
			this.enabled = enabled;
		}
	}
}
