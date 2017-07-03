package com.tle.common.settings.standard;

import com.tle.beans.item.VersionSelection;
import com.tle.common.settings.ConfigurationProperties;
import com.tle.common.settings.annotation.Property;

/**
 * @author larry
 */
public class QuickContributeAndVersionSettings implements ConfigurationProperties
{
	private static final long serialVersionUID = 1069120212116280127L;

	@Property(key = "one.click.collection")
	private String oneClickCollection;
	@Property(key = "version.selection")
	private VersionSelection versionSelection;
	@Property(key = "select.summary.page.button.disable")
	private boolean buttonDisable;

	public void setOneClickCollection(String oneClickCollection)
	{
		this.oneClickCollection = oneClickCollection;
	}

	public String getOneClickCollection()
	{
		return oneClickCollection;
	}

	public void setVersionSelection(VersionSelection versionSelection)
	{
		this.versionSelection = versionSelection;
	}

	public VersionSelection getVersionSelection()
	{
		return versionSelection;
	}

	public boolean isButtonDisable()
	{
		return buttonDisable;
	}

	public void setButtonDisable(boolean buttonDisable)
	{
		this.buttonDisable = buttonDisable;
	}
}
