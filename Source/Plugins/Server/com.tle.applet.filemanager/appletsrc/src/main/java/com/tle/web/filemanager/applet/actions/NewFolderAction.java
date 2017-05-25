package com.tle.web.filemanager.applet.actions;

import java.awt.event.KeyEvent;

import com.tle.web.filemanager.applet.FileListPanel;
import com.tle.web.filemanager.applet.backend.Backend;
import com.tle.web.filemanager.common.FileInfo;

public class NewFolderAction extends AbstractNamingAction
{
	private final Backend backend;

	public NewFolderAction(Backend backend, FileListPanel fileList)
	{
		super(fileList, "action.newfolder.", "newfolder.gif"); //$NON-NLS-1$ //$NON-NLS-2$
		setMnemonic(KeyEvent.VK_N);
		this.backend = backend;
	}

	@Override
	protected String getOriginalFileName(FileInfo selectedFile)
	{
		return null;
	}

	@Override
	protected void doAction(FileInfo selectedFile, String newName)
	{
		backend.newFolder(new FileInfo(getFileList().getCurrentDirectory(), newName));
	}
}
