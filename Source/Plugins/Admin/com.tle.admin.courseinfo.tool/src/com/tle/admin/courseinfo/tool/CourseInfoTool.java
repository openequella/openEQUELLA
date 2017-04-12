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
		return CurrentLocale.get("com.tle.admin.gui.courseinfotool.name"); //$NON-NLS-1$
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
