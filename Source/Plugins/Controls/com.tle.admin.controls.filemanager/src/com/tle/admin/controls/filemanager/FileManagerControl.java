package com.tle.admin.controls.filemanager;

import com.dytech.edge.wizard.beans.control.CustomControl;

public class FileManagerControl extends CustomControl
{
	private static final long serialVersionUID = -1129109182853406172L;
	private static final String AUTO_MARK_AS_RESOURCE = "autoMarkAsResource"; //$NON-NLS-1$
	private static final String ALLOW_WEBDAV = "allowWebDav"; //$NON-NLS-1$

	public FileManagerControl()
	{
		setClassType("filemanager"); //$NON-NLS-1$
	}

	public boolean isAutoMarkAsResource()
	{
		Boolean b = (Boolean) getAttributes().get(AUTO_MARK_AS_RESOURCE);
		return b == null || b.booleanValue();
	}

	public void setAutoMarkAsResource(boolean b)
	{
		getAttributes().put(AUTO_MARK_AS_RESOURCE, b);
	}

	public boolean isAllowWebDav()
	{
		Boolean b = (Boolean) getAttributes().get(ALLOW_WEBDAV);
		return b == null || b.booleanValue();
	}

	public void setAllowWebDav(boolean b)
	{
		getAttributes().put(ALLOW_WEBDAV, b);
	}
}
