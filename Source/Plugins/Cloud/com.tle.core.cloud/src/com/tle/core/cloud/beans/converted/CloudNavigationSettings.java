package com.tle.core.cloud.beans.converted;

import com.tle.beans.item.attachments.INavigationSettings;

/**
 * @author Aaron
 */
public class CloudNavigationSettings implements INavigationSettings
{
	private boolean showSplitOption;
	private boolean manualNavigation;

	@Override
	public boolean isShowSplitOption()
	{
		return showSplitOption;
	}

	public void setShowSplitOption(boolean showSplitOption)
	{
		this.showSplitOption = showSplitOption;
	}

	@Override
	public boolean isManualNavigation()
	{
		return manualNavigation;
	}

	public void setManualNavigation(boolean manualNavigation)
	{
		this.manualNavigation = manualNavigation;
	}
}
