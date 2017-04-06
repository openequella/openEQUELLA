package com.tle.admin.cal;

import net.miginfocom.swing.MigLayout;

import com.tle.admin.collection.summarydisplay.AbstractOnlyTitleConfig;

public class CalSummaryDisplayConfig extends AbstractOnlyTitleConfig
{
	@Override
	public void setup()
	{
		setLayout(new MigLayout());
		super.setup();
	}

	@Override
	public boolean showTitleHelp()
	{
		return true;
	}
}
