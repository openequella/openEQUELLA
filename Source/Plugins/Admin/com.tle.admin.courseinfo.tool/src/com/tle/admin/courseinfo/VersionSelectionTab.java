package com.tle.admin.courseinfo;

import java.awt.Component;
import java.awt.FlowLayout;

import com.tle.admin.baseentity.BaseEntityTab;
import com.tle.admin.gui.EditorException;
import com.tle.admin.gui.item.VersionSelectionConfig;
import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.common.i18n.CurrentLocale;

public class VersionSelectionTab extends BaseEntityTab<CourseInfo>
{
	private VersionSelectionConfig config;

	@Override
	@SuppressWarnings("deprecation")
	public void init(Component parent)
	{
		config = new VersionSelectionConfig(true);

		setLayout(new FlowLayout(FlowLayout.LEFT));
		add(config);
	}

	@Override
	@SuppressWarnings("nls")
	public String getTitle()
	{
		return CurrentLocale.get("com.tle.admin.courseinfo.versionstab.title");
	}

	@Override
	public void load()
	{
		config.load(state.getEntity().getVersionSelection());
	}

	@Override
	public void save()
	{
		state.getEntity().setVersionSelection(config.save());
	}

	@Override
	public void validation() throws EditorException
	{
		// Nothing to validate
	}
}
