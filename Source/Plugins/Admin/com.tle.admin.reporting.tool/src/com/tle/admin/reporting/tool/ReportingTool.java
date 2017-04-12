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
