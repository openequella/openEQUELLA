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

import java.util.ArrayList;
import java.util.List;

import com.tle.admin.baseentity.AccessControlTab;
import com.tle.admin.baseentity.BaseEntityEditor;
import com.tle.admin.baseentity.BaseEntityTab;
import com.tle.admin.tools.common.BaseEntityTool;
import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.PrivilegeTree.Node;

/**
 * @author Nicholas Read
 */
public class CourseInfoEditor extends BaseEntityEditor<CourseInfo>
{
	/**
	 * Constructs a new SchemaManager.
	 */
	public CourseInfoEditor(BaseEntityTool<CourseInfo> tool, boolean readonly)
	{
		super(tool, readonly);
	}

	@Override
	protected String getEntityName()
	{
		return "course";
	}

	@Override
	public String getDocumentName()
	{
		return getString("editor.docname"); //$NON-NLS-1$
	}

	@Override
	protected String getWindowTitle()
	{
		return getString("editor.title"); //$NON-NLS-1$
	}

	@Override
	protected List<BaseEntityTab<CourseInfo>> getTabs()
	{
		List<BaseEntityTab<CourseInfo>> tabs1 = new ArrayList<BaseEntityTab<CourseInfo>>();
		tabs1.add((DetailsTab) detailsTab);
		tabs1.add(new VersionSelectionTab());
		tabs1.add(new AccessControlTab<CourseInfo>(Node.COURSE_INFO));
		return tabs1;
	}

	@Override
	protected AbstractDetailsTab<CourseInfo> constructDetailsTab()
	{
		return new DetailsTab();
	}
}
