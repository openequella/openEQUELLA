package com.tle.web.filemanager.applet.actions;

import java.awt.event.KeyEvent;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import com.tle.common.i18n.CurrentLocale;
import com.tle.web.filemanager.applet.FileListPanel;
import com.tle.web.filemanager.applet.backend.Backend;
import com.tle.web.filemanager.common.FileInfo;

public class RenameAction extends AbstractNamingAction
{
	private final Backend backend;

	public RenameAction(Backend backend, FileListPanel fileList)
	{
		super(fileList, "action.rename.", "rename.gif"); //$NON-NLS-1$ //$NON-NLS-2$
		setMnemonic(KeyEvent.VK_R);
		this.backend = backend;
	}

	@Override
	protected String getOriginalFileName(FileInfo selectedFile)
	{
		return selectedFile.getName();
	}

	@Override
	protected void doAction(FileInfo selectedFile, String newName)
	{
		if( !backend.move(selectedFile, new FileInfo(selectedFile.getParentFileInfo(), newName)) )
		{
			JOptionPane.showMessageDialog(getFileList(), CurrentLocale.get(keyPrefix + "failure"), CurrentLocale //$NON-NLS-1$
				.get(keyPrefix + "dialog.popuptitle"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
		}
	}

	@Override
	public void update()
	{
		setEnabled(getFileList().getSelectionCount() == 1);
	}

	@Override
	public KeyStroke invokeForWindowKeyStroke()
	{
		return KeyStroke.getKeyStroke("F2"); //$NON-NLS-1$
	}
}
