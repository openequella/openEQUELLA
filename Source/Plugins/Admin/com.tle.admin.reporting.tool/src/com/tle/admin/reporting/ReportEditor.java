/*
 * Created on May 10, 2005
 */
package com.tle.admin.reporting;

import java.util.ArrayList;
import java.util.List;

import com.tle.admin.baseentity.AccessControlTab;
import com.tle.admin.baseentity.BaseEntityEditor;
import com.tle.admin.baseentity.BaseEntityTab;
import com.tle.admin.tools.common.BaseEntityTool;
import com.tle.beans.entity.report.Report;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.PrivilegeTree.Node;

/**
 * @author Nicholas Read
 */
public class ReportEditor extends BaseEntityEditor<Report>
{
	private static final String KEYPFX = "com.tle.admin.reporting.tool."; //$NON-NLS-1$

	/**
	 * Constructs a new SchemaManager.
	 */
	public ReportEditor(BaseEntityTool<Report> tool, boolean readonly)
	{
		super(tool, readonly);
	}

	@Override
	protected String getEntityName()
	{
		return "report"; //$NON-NLS-1$
	}

	@Override
	public String getDocumentName()
	{
		return CurrentLocale.get(KEYPFX + "reporteditor.docname"); //$NON-NLS-1$
	}

	@Override
	protected String getWindowTitle()
	{
		return CurrentLocale.get(KEYPFX + "reporteditor.title"); //$NON-NLS-1$
	}

	@Override
	protected List<BaseEntityTab<Report>> getTabs()
	{
		List<BaseEntityTab<Report>> tabs = new ArrayList<BaseEntityTab<Report>>();
		tabs.add((DetailsTab) detailsTab);
		tabs.add(new AccessControlTab<Report>(Node.REPORT));
		return tabs;
	}

	@Override
	protected AbstractDetailsTab<Report> constructDetailsTab()
	{
		return new DetailsTab();
	}
}
