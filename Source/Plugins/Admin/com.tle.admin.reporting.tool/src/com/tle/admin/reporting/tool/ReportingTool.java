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

package com.tle.admin.reporting.tool;

import com.tle.admin.baseentity.BaseEntityEditor;
import com.tle.admin.reporting.ReportEditor;
import com.tle.admin.tools.common.BaseEntityTool;
import com.tle.beans.entity.report.Report;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.reporting.RemoteReportingService;
import com.tle.core.remoting.RemoteAbstractEntityService;

public class ReportingTool extends BaseEntityTool<Report>
{
	public ReportingTool() throws Exception
	{
		super(Report.class, RemoteReportingService.ENTITY_TYPE);
	}

	@Override
	protected RemoteAbstractEntityService<Report> getService(ClientService client)
	{
		return client.getService(RemoteReportingService.class);
	}

	@Override
	protected String getErrorPath()
	{
		return "reporting";
	}

	@Override
	protected BaseEntityEditor<Report> createEditor(boolean readonly)
	{
		return new ReportEditor(this, readonly);
	}

	@Override
	protected String getEntityName()
	{
		return CurrentLocale.get("com.tle.admin.gui.reportingtool.title");
	}

}
