package com.tle.web.cloud.view;

import com.tle.web.selection.section.SelectionSummarySection;

@SuppressWarnings("nls")
public class CloudViewItemSelectionSummarySection extends SelectionSummarySection
{
	public CloudViewItemSelectionSummarySection()
	{
		setLayout("");
		setFollowWithHr(true);
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "ss";
	}
}
