package com.tle.web.filemanager.applet.gui;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;

import com.tle.web.filemanager.common.FileInfo;

public final class SystemIconCache
{
	private static final Map<String, Icon> CACHE = new ConcurrentHashMap<String, Icon>();

	private static final String FOLDER_LARGE = "FOLDER_LARGE"; //$NON-NLS-1$
	private static final String FOLDER_SMALL = "FOLDER_SMALL"; //$NON-NLS-1$

	private static Icon failureFileIcon = UIManager.getIcon("FileView.fileIcon"); //$NON-NLS-1$
	private static Icon failureFolderIcon = UIManager.getIcon("FileView.directoryIcon"); //$NON-NLS-1$

	public static Icon getIcon(FileInfo info, boolean large)
	{
		String extension = null;
		if( info.isDirectory() )
		{
			extension = large ? FOLDER_LARGE : FOLDER_SMALL;
		}
		else
		{
			String filename = info.getName();
			int ind = filename.lastIndexOf('.');
			extension = (ind < 0 ? filename : filename.substring(ind)).toLowerCase();
		}

		Icon icon = null;
		if( CACHE.containsKey(extension) )
		{
			icon = CACHE.get(extension);
		}
		else
		{
			File temp = null;
			try
			{
				temp = File.createTempFile("icon", extension); //$NON-NLS-1$;
				File temp2 = info.isDirectory() ? temp.getParentFile() : temp;
				try
				{
					sun.awt.shell.ShellFolder shellFolder = sun.awt.shell.ShellFolder.getShellFolder(temp2);
					icon = new ImageIcon(shellFolder.getIcon(large));
				}
				catch( Exception ex )
				{
					FileSystemView fsView = FileSystemView.getFileSystemView();
					icon = fsView.getSystemIcon(temp2);

					// Ensure that we get a directory icon if it is a directory,
					// and the thing doesn't work
					if( icon.equals(failureFileIcon) && info.isDirectory() )
					{
						icon = failureFolderIcon;
					}
				}
			}
			catch( IOException ex )
			{
				icon = info.isDirectory() ? failureFolderIcon : failureFileIcon;
			}
			finally
			{
				if( temp != null )
				{
					temp.delete();
				}
			}

			CACHE.put(extension, icon);
		}

		return icon;
	}

	private SystemIconCache()
	{
		throw new Error();
	}
}
