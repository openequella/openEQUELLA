package com.tle.web.filemanager.applet.actions;

import java.awt.ComponentOrientation;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.common.i18n.CurrentLocale;
import com.tle.web.appletcommon.gui.GlassProgressWorker;
import com.tle.web.filemanager.applet.FileListPanel;
import com.tle.web.filemanager.applet.backend.Backend;
import com.tle.web.filemanager.common.FileInfo;

public class DeleteAction extends TLEAction
{
	private final Backend backend;
	private final FileListPanel fileList;

	public DeleteAction(Backend backend, FileListPanel fileList)
	{
		super(CurrentLocale.get("action.delete.name")); //$NON-NLS-1$

		setIcon("delete.gif"); //$NON-NLS-1$
		setShortDescription(CurrentLocale.get("action.delete.desc")); //$NON-NLS-1$
		setMnemonic(KeyEvent.VK_D);

		this.backend = backend;
		this.fileList = fileList;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		final List<FileInfo> selected = fileList.getSelectedFiles();

		if( confirmDelete(selected) )
		{
			GlassProgressWorker<?> worker = new GlassProgressWorker<Object>(
				CurrentLocale.get("action.delete.progress.unknown"), selected.size(), false) //$NON-NLS-1$
			{
				@Override
				public Object construct() throws Exception
				{
					for( FileInfo info : selected )
					{
						setMessage(CurrentLocale.get("action.delete.progress.deleting", info //$NON-NLS-1$
							.getName()));
						backend.delete(info);
						addProgress(1);
					}
					return null;
				}

			};
			worker.setComponent(fileList);
			worker.start();
		}
	}

	private boolean confirmDelete(List<FileInfo> selected)
	{
		String confirmKey = "multiple"; //$NON-NLS-1$
		Object confirmArg = selected.size();
		if( selected.size() == 1 )
		{
			FileInfo file = selected.get(0);
			if( file.isDirectory() )
			{
				confirmKey = "singlefolder"; //$NON-NLS-1$
			}
			else
			{
				confirmKey = "singlefile"; //$NON-NLS-1$
			}
			confirmArg = file.getName();
		}
		confirmKey = "action.delete.confirm." + confirmKey; //$NON-NLS-1$

		if( CurrentLocale.isRightToLeft() )
		{
			fileList.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		}

		return JOptionPane.showConfirmDialog(fileList,
			CurrentLocale.get(confirmKey + ".message", confirmArg), CurrentLocale.get(confirmKey + ".title"), //$NON-NLS-1$ //$NON-NLS-2$
			JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
	}

	@Override
	public void update()
	{
		setEnabled(fileList.getSelectionCount() >= 1);
	}

	@Override
	public KeyStroke invokeForWindowKeyStroke()
	{
		return KeyStroke.getKeyStroke("DELETE"); //$NON-NLS-1$
	}
}