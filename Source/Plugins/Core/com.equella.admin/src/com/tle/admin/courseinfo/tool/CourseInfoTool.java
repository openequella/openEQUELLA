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

package com.tle.admin.courseinfo.tool;

import java.awt.Component;
import java.util.List;

import com.tle.admin.baseentity.BaseEntityEditor;
import com.tle.admin.common.gui.actions.BulkAction;
import com.tle.admin.courseinfo.CourseInfoEditor;
import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.admin.tools.common.BaseEntityTool;
import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.remoting.RemoteAbstractEntityService;
import com.tle.core.remoting.RemoteCourseInfoService;

public class CourseInfoTool extends BaseEntityTool<CourseInfo>
{
	public CourseInfoTool() throws Exception
	{
		super(CourseInfo.class, RemoteCourseInfoService.ENTITY_TYPE);
	}

	@Override
	protected RemoteAbstractEntityService<CourseInfo> getService(ClientService client)
	{
		return client.getService(RemoteCourseInfoService.class);
	}

	@Override
	protected BaseEntityEditor<CourseInfo> createEditor(boolean readonly)
	{
		return new CourseInfoEditor(this, readonly);
	}

	@Override
	protected String getEntityName()
	{
		return getString("courses.name"); //$NON-NLS-1$
	}

	@Override
	protected String getErrorPath()
	{
		return "courseInfo"; //$NON-NLS-1$
	}

	@Override
	protected void getButtonActions(List<TLEAction> actions)
	{
		super.getButtonActions(actions);
		actions.add(bulkAction);
		actions.add(archiveAction);
		actions.add(unarchiveAction);
	}

	private final TLEAction bulkAction = new BulkAction<CourseInfo>()
	{
		private static final long serialVersionUID = 1L;

		@Override
		protected void refresh()
		{
			refreshAndSelect();
		}

		@Override
		protected void bulkImport(byte[] array, boolean override) throws Exception
		{
			clientService.getService(RemoteCourseInfoService.class).bulkImport(array, override);
		}

		@Override
		protected Component getParent()
		{
			return parentFrame;
		}
	};
}
