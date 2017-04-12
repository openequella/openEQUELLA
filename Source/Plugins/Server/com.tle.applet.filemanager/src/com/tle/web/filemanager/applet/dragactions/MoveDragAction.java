package com.tle.web.filemanager.applet.dragactions;

import com.tle.common.i18n.CurrentLocale;
import com.tle.web.filemanager.applet.backend.Backend;
import com.tle.web.filemanager.applet.dragactions.InternalDragHandler.InternalDragAction;
import com.tle.web.filemanager.common.FileInfo;

public class MoveDragAction implements InternalDragAction
{
	private final Backend backend;

	public MoveDragAction(Backend backend)
	{
		this.backend = backend;
	}

	@Override
	public void doAction(FileInfo draggedFile, FileInfo targetDir)
	{
		backend.move(draggedFile, new FileInfo(targetDir, draggedFile.getName()));
	}

	@Override
	public String getDisplayName()
	{
		return CurrentLocale.get("dragaction.move.name"); //$NON-NLS-1$
	}

	@Override
	public String getIcon()
	{
		return "move.gif"; //$NON-NLS-1$
	}
}
