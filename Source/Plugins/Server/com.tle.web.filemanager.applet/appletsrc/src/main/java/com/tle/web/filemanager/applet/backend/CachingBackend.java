package com.tle.web.filemanager.applet.backend;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import com.dytech.common.text.NumberStringComparator;
import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.tle.common.i18n.CurrentLocale;
import com.tle.web.appletcommon.io.ProgressMonitorCallback;
import com.tle.web.appletcommon.io.ProgressMonitorInputStream;
import com.tle.web.appletcommon.io.ProgressMonitorOutputStream;
import com.tle.web.filemanager.common.FileInfo;

/**
 * Provides caching of some data in order to reduce the amount of network trips
 * required.
 * 
 * @author Nicholas Read
 */
public class CachingBackend implements Backend
{
	private static final long FILE_WATCH_INVERVAL = TimeUnit.SECONDS.toMillis(5);

	private final Timer fileWatchTimer;

	private final AbstractRemoteBackendImpl backend;
	private final Map<String, List<FileInfo>> dirListCache = new HashMap<String, List<FileInfo>>();
	protected final Map<String, CachedFile> cachedFiles = new HashMap<String, CachedFile>();
	protected final Set<BackendListener> listeners = new HashSet<BackendListener>();

	public CachingBackend(AbstractRemoteBackendImpl backend)
	{
		this.backend = backend;

		fileWatchTimer = new Timer(true);
		fileWatchTimer.schedule(new FileWatcherTask(), FILE_WATCH_INVERVAL, FILE_WATCH_INVERVAL);
	}

	@Override
	public final void addListener(BackendListener listener)
	{
		listeners.add(listener);
	}

	@Override
	public synchronized List<FileInfo> listFiles(String directory)
	{
		List<FileInfo> files = dirListCache.get(directory.toLowerCase());
		if( files == null )
		{
			files = backend.listFiles(directory);
			Collections.sort(files, new NumberStringComparator<FileInfo>()
			{
				private static final long serialVersionUID = 1L;

				@Override
				public int compare(FileInfo f1, FileInfo f2)
				{
					if( f1.isDirectory() && !f2.isDirectory() )
					{
						return -1;
					}
					else if( !f1.isDirectory() && f2.isDirectory() )
					{
						return 1;
					}
					else
					{
						return super.compare(f1, f2);
					}
				}

				@Override
				public String convertToString(FileInfo t)
				{
					return t.getName();
				}
			});
			dirListCache.put(directory.toLowerCase(), files);
		}
		return files;
	}

	@Override
	public boolean isCachedLocally(FileInfo info)
	{
		return cachedFiles.containsKey(info.getFullPath().toLowerCase());
	}

	@Override
	public File cacheFileForEditing(FileInfo info, ProgressMonitorCallback callback) throws IOException
	{
		final String fullpath = info.getFullPath();

		CachedFile cachedFile = cachedFiles.get(fullpath.toLowerCase());

		if( cachedFile == null )
		{
			File file = File.createTempFile("equella-", '-' + info.getName()); //$NON-NLS-1$
			file.deleteOnExit();

			try( InputStream in = backend.readFile(fullpath);
				OutputStream out = new ProgressMonitorOutputStream(new FileOutputStream(file), callback) )
			{
				ByteStreams.copy(in, out);
			}

			cachedFile = new CachedFile(info, file);
			cachedFiles.put(fullpath.toLowerCase(), cachedFile);
		}
		return cachedFile.getFile();
	}

	@Override
	public void downloadFile(FileInfo info, File destination, ProgressMonitorCallback callback) throws IOException
	{
		InputStream in = null;
		try
		{
			// Ensure the parent directory exists
			destination.getParentFile().mkdirs();

			if( isCachedLocally(info) )
			{
				in = new FileInputStream(cachedFiles.get(info.getFullPath().toLowerCase()).getFile());
			}
			else
			{
				in = backend.readFile(info.getFullPath());
			}

			in = new ProgressMonitorInputStream(new BufferedInputStream(in), callback);
			try( OutputStream out = new BufferedOutputStream(new FileOutputStream(destination)) )
			{
				ByteStreams.copy(in, out);
			}
		}
		finally
		{
			Closeables.close(in, false);
		}
	}

	@Override
	public long uploadFile(File file, FileInfo cwd, ProgressMonitorCallback progressMonitorCallback) throws IOException
	{
		FileInfo newFile = new FileInfo(cwd, file.getName());

		invalidateParentFolder(newFile);
		try( InputStream in = new ProgressMonitorInputStream(new BufferedInputStream(new FileInputStream(file)),
			progressMonitorCallback) )
		{
			backend.writeFile(newFile.getFullPath(), file.length(), in);
		}

		cachedFiles.remove(newFile.getFullPath().toLowerCase());

		fireEvent("fileAdded", new BackendEvent(newFile)); //$NON-NLS-1$
		return file.length();
	}

	@Override
	public List<FileInfo> getUnsynchronisedFiles()
	{
		List<FileInfo> results = new ArrayList<FileInfo>();
		for( CachedFile file : cachedFiles.values() )
		{
			if( file.hasChangedSinceLastSync() )
			{
				results.add(file.getFileInfo());
			}
		}
		return results;
	}

	@Override
	public void synchroniseFile(FileInfo info, ProgressMonitorCallback callback) throws IOException
	{
		final String fullpath = info.getFullPath();

		CachedFile cachedFile = cachedFiles.get(fullpath.toLowerCase());
		if( cachedFile == null )
		{
			throw new RuntimeException(CurrentLocale.get("caching.notcached", fullpath)); //$NON-NLS-1$
		}

		try( InputStream in = new BufferedInputStream(new ProgressMonitorInputStream(new FileInputStream(
			cachedFile.getFile()), callback)) )
		{
			backend.writeFile(fullpath, cachedFile.getFile().length(), in);

			cachedFile.setAsSynced();
			cachedFiles.put(fullpath.toLowerCase(), cachedFile);

			invalidateParentFolder(info);
		}

		fireEvent("localFilesEdited", null); //$NON-NLS-1$
	}

	@Override
	public synchronized void delete(FileInfo info)
	{
		try
		{
			List<FileInfo> cache = dirListCache.get(info.getPath().toLowerCase());
			if( cache != null )
			{
				cache.remove(info);
			}
			backend.delete(info);
			fireEvent("fileDeleted", new BackendEvent(info)); //$NON-NLS-1$
		}
		catch( RuntimeException ex )
		{
			// Unknown status - clear the directory cache
			invalidateParentFolder(info);
			throw ex;
		}
	}

	@Override
	public boolean move(FileInfo sourceFile, FileInfo destinationFile)
	{
		invalidateParentFolder(sourceFile);
		invalidateParentFolder(destinationFile);
		if( backend.move(sourceFile, destinationFile) )
		{
			fireEvent("fileMoved", new BackendEvent(sourceFile, destinationFile)); //$NON-NLS-1$
			return true;
		}
		return false;
	}

	@Override
	public void copy(FileInfo sourceFile, FileInfo destFile)
	{
		invalidateParentFolder(destFile);
		backend.copy(sourceFile, destFile);
		fireEvent("fileCopied", new BackendEvent(sourceFile, destFile)); //$NON-NLS-1$
	}

	@Override
	public void toggleMarkAsResource(FileInfo info)
	{
		invalidateParentFolder(info);
		backend.toggleMarkAsResource(info);
		fireEvent("fileMarkedAsResource", new BackendEvent(info)); //$NON-NLS-1$
	}

	@Override
	public void newFolder(FileInfo info)
	{
		invalidateParentFolder(info);
		backend.newFolder(info);
		fireEvent("fileAdded", new BackendEvent(info)); //$NON-NLS-1$
	}

	@Override
	public void extractArchive(FileInfo info)
	{
		invalidateParentFolder(info);
		backend.extractArchive(info);
		fireEvent("extractArchive", new BackendEvent(info)); //$NON-NLS-1$
	}

	private synchronized void invalidateParentFolder(FileInfo info)
	{
		dirListCache.remove(Strings.nullToEmpty(info.getPath()).toLowerCase());
	}

	protected void fireEvent(final String methodName, final BackendEvent event)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					Method method = BackendListener.class.getMethod(methodName, BackendEvent.class);
					for( BackendListener l : listeners )
					{
						method.invoke(l, event);
					}
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}
		});
	}

	private static class CachedFile
	{
		private final FileInfo info;
		private final File file;

		private long lastSynced;

		public CachedFile(FileInfo info, File file)
		{
			this.info = info;
			this.file = file;

			setAsSynced();
		}

		public FileInfo getFileInfo()
		{
			return info;
		}

		public File getFile()
		{
			return file;
		}

		public boolean hasChangedSinceLastSync()
		{
			return lastSynced != file.lastModified();
		}

		public void setAsSynced()
		{
			lastSynced = file.lastModified();
		}
	}

	protected class FileWatcherTask extends TimerTask
	{

		@Override
		public void run()
		{
			for( CachedFile file : cachedFiles.values() )
			{
				if( file.hasChangedSinceLastSync() )
				{
					fireEvent("localFilesEdited", null); //$NON-NLS-1$
					return;
				}
			}
		}
	}
}
