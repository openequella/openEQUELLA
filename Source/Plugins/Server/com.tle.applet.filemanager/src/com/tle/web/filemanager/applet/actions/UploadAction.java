package com.tle.web.filemanager.applet.actions;

import java.awt.ComponentOrientation;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.common.i18n.CurrentLocale;
import com.tle.web.appletcommon.dnd.DnDUtils;
import com.tle.web.appletcommon.dnd.DropHandler;
import com.tle.web.appletcommon.gui.GlassProgressWorker;
import com.tle.web.appletcommon.gui.InterruptAwareGlassPaneProgressMonitorCallback;
import com.tle.web.appletcommon.gui.UserCancelledException;
import com.tle.web.appletcommon.io.FileSize;
import com.tle.web.filemanager.applet.FileListPanel;
import com.tle.web.filemanager.applet.backend.Backend;
import com.tle.web.filemanager.common.FileInfo;

public class UploadAction extends TLEAction implements DropHandler
{
	private final FileListPanel fileList;
	private final Backend backend;
	private final boolean autoMarkAsResources;

	private File lastSelectedDirectory;

	public UploadAction(Backend backend, FileListPanel fileList, boolean autoMarkAsResources)
	{
		super(CurrentLocale.get("action.upload.name")); //$NON-NLS-1$

		setShortDescription(CurrentLocale.get("action.upload.desc")); //$NON-NLS-1$
		setIcon("upload.gif"); //$NON-NLS-1$

		this.backend = backend;
		this.fileList = fileList;
		this.autoMarkAsResources = autoMarkAsResources;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		JFileChooser fc = new JFileChooser(lastSelectedDirectory);
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.setMultiSelectionEnabled(true);

		if( fc.showOpenDialog(fileList) == JFileChooser.APPROVE_OPTION )
		{
			File[] selectedFiles = fc.getSelectedFiles();
			if( selectedFiles != null && selectedFiles.length > 0 )
			{
				lastSelectedDirectory = selectedFiles[0];
				if( !lastSelectedDirectory.isDirectory() )
				{
					lastSelectedDirectory = lastSelectedDirectory.getParentFile();
				}

				uploadFiles(fileList.getCurrentDirectory(), selectedFiles);
			}
		}
	}

	private void uploadFiles(final FileInfo directory, final File[] files)
	{
		if( Utils.filenamesClash(files, backend.listFiles(directory.getFullPath())) )
		{
			if( !Utils.confirmOverwrite(fileList, "action.upload.overwrite.msg") ) //$NON-NLS-1$
			{
				return;
			}
		}

		GlassProgressWorker<?> worker = new GlassProgressWorker<Object>(
			CurrentLocale.get("action.upload.progress.unknown"), (int) FileSize.getFileSize(files), true) //$NON-NLS-1$
		{
			@Override
			public Object construct() throws Exception
			{
				uploadFiles(directory, files);
				return null;
			}

			private void uploadFiles(FileInfo cwd, File[] files) throws IOException
			{
				final boolean markAsRes = autoMarkAsResources && cwd.isRoot();
				for( File file : files )
				{
					if( !file.isDirectory() )
					{
						setMessage(CurrentLocale.get("action.upload.progress.uploading", file //$NON-NLS-1$
							.getName()));
						long size = backend.uploadFile(file, cwd, new InterruptAwareGlassPaneProgressMonitorCallback(
							this));

						if( size > 0 && markAsRes )
						{
							backend.toggleMarkAsResource(new FileInfo(cwd, file.getName()));
						}
					}
					else
					{
						FileInfo newdir = new FileInfo(cwd, file.getName());
						backend.newFolder(newdir);
						uploadFiles(newdir, file.listFiles());
					}
				}
			}

			@Override
			public void exception()
			{
				Exception ex = getException();
				if( !(ex instanceof UserCancelledException) )
				{
					ex.printStackTrace(System.out);
					System.out.flush();
					if( CurrentLocale.isRightToLeft() )
					{
						fileList.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
					}
					JOptionPane.showMessageDialog(fileList,
						CurrentLocale.get("action.upload.error.message"), CurrentLocale //$NON-NLS-1$
							.get("action.upload.error.title"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
				}
			}
		};
		worker.setComponent(fileList);
		worker.start();
	}

	@Override
	public int getDropHandlerPriority()
	{
		return 0;
	}

	@Override
	public boolean supportsDrop(DropTargetDragEvent e)
	{
		return DnDUtils.supportsNativeFileDrop(e);
	}

	@Override
	public boolean handleDrop(DropTargetDropEvent e) throws Exception
	{
		List<File> files = DnDUtils.getDroppedNativeFiles(e.getTransferable());
		try
		{
			FileInfo dir = fileList.getFileUnderMouseCursor();
			if( dir == null || !dir.isDirectory() )
			{
				dir = fileList.getCurrentDirectory();
			}

			uploadFiles(dir, files.toArray(new File[files.size()]));
			return true;
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
			return false;
		}
	}
}