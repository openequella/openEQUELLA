package com.tle.web.payment.shop.section.search;

import com.tle.web.search.actions.SearchActionsSection;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;

public class ShopSearchActionsSection extends SearchActionsSection
{
	@Override
	public String[] getResetFilterAjaxIds()
	{
		// Annoying
		return new String[]{SearchResultsActionsSection.ACTIONS_AJAX_ID, SearchResultsActionsSection.BUTTON_AJAX_ID};
	}
}
