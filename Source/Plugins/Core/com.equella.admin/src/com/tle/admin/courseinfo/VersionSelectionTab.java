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
		return getString("versionstab.title");
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
