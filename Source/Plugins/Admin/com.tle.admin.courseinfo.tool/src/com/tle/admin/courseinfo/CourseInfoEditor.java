/*
 * Created on May 10, 2005
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
		return CurrentLocale.get("com.tle.admin.courseinfo.editor.docname"); //$NON-NLS-1$
	}

	@Override
	protected String getWindowTitle()
	{
		return CurrentLocale.get("com.tle.admin.courseinfo.editor.title"); //$NON-NLS-1$
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
