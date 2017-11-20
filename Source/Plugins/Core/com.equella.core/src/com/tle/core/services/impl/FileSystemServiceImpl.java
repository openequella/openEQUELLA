/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.services.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.tle.core.plugins.AbstractPluginService;
import com.tle.core.services.ZipProgress;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dytech.common.io.FileUtils;
import com.dytech.common.io.FileUtils.GrepFunctor;
import com.dytech.devlib.Md5;
import com.dytech.edge.common.FileInfo;
import com.dytech.edge.exceptions.BannedFileException;
import com.dytech.edge.exceptions.FileSystemException;
import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.tle.beans.Institution;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.filesystem.FileCallback;
import com.tle.common.filesystem.FileEntry;
import com.tle.common.filesystem.FileSystemHelper;
import com.tle.common.filesystem.Filestore;
import com.tle.common.filesystem.handle.ConversionFile;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.common.filesystem.handle.TrashFile;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.quota.settings.QuotaSettings;
import com.tle.core.events.services.EventService;
import com.tle.core.filesystem.InstitutionFile;
import com.tle.core.guice.Bind;
import com.tle.core.healthcheck.listeners.ServiceCheckRequestListener;
import com.tle.core.healthcheck.listeners.ServiceCheckResponseListener.CheckServiceResponseEvent;
import com.tle.core.healthcheck.listeners.bean.ServiceStatus;
import com.tle.core.healthcheck.listeners.bean.ServiceStatus.ServiceName;
import com.tle.core.healthcheck.listeners.bean.ServiceStatus.Status;
import com.tle.core.services.FileSystemService;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.core.util.archive.ArchiveCreator;
import com.tle.core.util.archive.ArchiveEntry;
import com.tle.core.util.archive.ArchiveExtractor;
import com.tle.core.util.archive.ArchiveProgress;
import com.tle.core.util.archive.ArchiveType;
import com.tle.core.zookeeper.ZookeeperService;
import com.tle.web.stream.FileContentStream;

/**
 * @author Nicholas Read
 */
@Bind(FileSystemService.class)
@Singleton
@SuppressWarnings("nls")
public class FileSystemServiceImpl implements FileSystemService, ServiceCheckRequestListener
{
	private static final Log LOGGER = LogFactory.getLog(FileSystemServiceImpl.class);
	private static final String DIGEST_MD5 = "md5";
	private static final String KEY_PFX = AbstractPluginService.getMyPluginId(FileSystemServiceImpl.class)+".";

	@Inject
	private ConfigurationService configService;
	@Inject
	private EventService eventService;
	@Inject
	private ZookeeperService zkService;
	@Inject
	private Map<String, Filestore> filestores;

	@com.google.inject.Inject(optional = true)
	@Named("filestore.advanced")
	private boolean advancedFilestore;
	@Inject
	@Named("filestore.root")
	private File rootDir;

	@PostConstruct
	private void init()
	{
		LOGGER.info("Filestore: " + this.rootDir.getAbsolutePath());
		LOGGER.info("Filestore advanced: " + advancedFilestore);

		if( advancedFilestore && filestores.size() > 0 )
		{
			LOGGER.info("Additional filestores: ");
			for( Entry<String, Filestore> filestore : filestores.entrySet() )
			{
				LOGGER.info(filestore.getValue().toString());
			}
		}
	}

	@Override
	public File getExternalFile(FileHandle handle, String path)
	{
		return getFile(handle, path);
	}

	private File getFile(FileHandle handle, String path)
	{
		File base = getFile(handle);
		if( Check.isEmpty(path) )
		{
			return base;
		}

		path = FileSystemHelper.encode(path);
		File child = new File(base, path);
		try
		{
			if( !child.getCanonicalPath().startsWith(base.getCanonicalPath()) )
			{
				throw new RuntimeException("Path attempts to traverse above the base directory: " + path);
			}
		}
		catch( IOException e )
		{
			throw new RuntimeException("Error getting file", e);
		}
		return child;
	}

	/**
	 * Returns the file for the given file handle.
	 */
	private File getFile(FileHandle handle)
	{
		prepareFileHandle(handle);
		final String filestoreId = handle.getFilestoreId();
		if( filestoreId != null )
		{
			final Filestore filestore = filestores.get(filestoreId);
			if( filestore == null )
			{
				throw new Error("Unknown filestore " + filestoreId);
			}
			return new File(filestore.getRoot().toFile(), handle.getAbsolutePath());
		}
		return new File(rootDir, handle.getAbsolutePath());
	}

	private void prepareFileHandle(FileHandle handle)
	{
		if( handle instanceof ConversionFile )
		{
			handle = ((ConversionFile) handle).getHandle();
		}

		if( handle instanceof InstitutionFile )
		{
			InstitutionFile h = (InstitutionFile) handle;
			if( h.getInstitution() == null )
			{
				h.setInstitution(CurrentInstitution.get());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.services.FileSystemService#enumerate(com.tle.core.filesystem
	 * .FileHandle, java.lang.String)
	 */
	@Override
	public FileEntry[] enumerate(FileHandle handle, String path, FileFilter filter)
	{
		return FileSystemHelper.listDir(getFile(handle, path), filter);
	}

	private void enumTree(final FileEntry parent, final File parentFile, final FileFilter filter)
	{
		final File[] files = FileSystemHelper.listDirFiles(parentFile, filter);
		if( files != null )
		{
			final List<FileEntry> fileEntries = new ArrayList<FileEntry>();

			Arrays.sort(files, new Comparator<File>()
			{
				@Override
				public int compare(File f1, File f2)
				{
					if( f1.isDirectory() != f2.isDirectory() )
					{
						return f1.isDirectory() ? -1 : 1;
					}
					else
					{
						return f1.compareTo(f2);
					}
				}
			});

			for( File file : files )
			{
				FileEntry newfile = new FileEntry(file);
				if( newfile.isFolder() )
				{
					enumTree(newfile, file, filter);
				}
				newfile.setLength(file.length());
				newfile.setName(FileSystemHelper.decode(newfile.getName()));
				fileEntries.add(newfile);
			}

			parent.setFiles(fileEntries);
		}
	}

	@Override
	public FileEntry enumerateTree(FileHandle handle, String path, FileFilter filter)
	{
		File rootFile = getFile(handle, path);
		FileEntry root = new FileEntry(rootFile);
		root.setName(FileSystemHelper.decode(rootFile.getName()));
		enumTree(root, rootFile, filter);
		return root;
	}

	@Override
	public long countFiles(FileHandle handle, String path)
	{
		return FileUtils.countFiles(getFile(handle, path).toPath());
	}

	/**
	 * Ensures that a filename is not banned.
	 */
	private void ensureNotBanned(String filename)
	{
		Institution institution = CurrentInstitution.get();
		if( institution != null )
		{
			Collection<String> banned = configService.getProperties(new QuotaSettings()).getBannedExtensions();
			if( banned != null && hasFileExtension(filename, banned) )
			{
				throw new BannedFileException(filename);
			}
		}
	}

	private void ensureNotStaging(FileHandle handle)
	{
		if( handle instanceof StagingFile )
		{
			throw new IllegalArgumentException("Argument can not be staging");
		}
	}

	@Override
	public InputStream read(FileHandle handle, String filename) throws IOException
	{
		File file = getFile(handle, filename);
		if( LOGGER.isDebugEnabled() )
		{
			LOGGER.debug("read: " + handle.getAbsolutePath() + '/' + filename);
		}
		return getInputStream(file);
	}

	@Override
	public OutputStream getOutputStream(FileHandle handle, String filename, boolean append) throws IOException
	{
		return getOutputStream(getFile(handle, filename), append);
	}

	@Override
	public FileInfo write(FileHandle handle, String filename, InputStream content, boolean append, boolean calculateMd5)
		throws IOException
	{
		ensureNotBanned(filename);
		File file = getFile(handle, filename);
		if( LOGGER.isDebugEnabled() )
		{
			LOGGER.debug("write: " + handle.getAbsolutePath() + '/' + filename);
		}

		try
		{
			Files.createDirectories(file.toPath().getParent());
		}
		catch( AccessDeniedException ex )
		{
			String serviceUser = System.getProperty("user.name");
			throw new IOException("Couldn't create directories on file store. Are you sure that EQUELLA running as '"
				+ serviceUser + "' user has the correct permissions?", ex);
		}

		MessageDigest md5 = null;
		if( calculateMd5 )
		{
			try
			{
				md5 = MessageDigest.getInstance(DIGEST_MD5);
			}
			catch( NoSuchAlgorithmException e )
			{
				throw new Error("Missing MD5 Digest");
			}
		}

		try( OutputStream out = getOutputStream(file, append) )
		{
			long byteCount = copyStream(content, out, md5);

			String md5Hex = null;
			if( md5 != null )
			{
				md5Hex = Md5.stringify(md5.digest());
			}
			return new FileInfo(byteCount, filename, md5Hex);
		}
	}

	@Override
	public FileInfo write(FileHandle handle, String filename, InputStream content, boolean append) throws IOException
	{
		return write(handle, filename, content, append, false);
	}

	public static long copyStream(InputStream source, OutputStream destination, MessageDigest md5) throws IOException
	{
		final int bufferSize = 4096;
		long copiedBytes = 0;
		byte buffer[] = new byte[bufferSize];
		for( int bytes = source.read(buffer, 0, buffer.length); bytes != -1; bytes = source.read(buffer, 0,
			buffer.length) )
		{
			destination.write(buffer, 0, bytes);
			if( md5 != null )
			{
				md5.update(buffer, 0, bytes);
			}
			copiedBytes += bytes;
		}
		return copiedBytes;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.services.FileSystemService#write(com.tle.core.filesystem
	 * .FileHandle, java.lang.String, java.io.InputStream, boolean)
	 */
	@Override
	public FileInfo write(FileHandle handle, String filename, Reader content, boolean append) throws IOException
	{
		ensureNotBanned(filename);

		File file = getFile(handle, filename);

		if( LOGGER.isDebugEnabled() )
		{
			LOGGER.debug("write: " + handle.getAbsolutePath() + filename);
		}

		if( !file.exists() )
		{
			file.getParentFile().mkdirs(); // NOSONAR - see mkdirs comment above
			if( !file.getParentFile().exists() )
			{
				throw new IOException("Could not create directory " + file.getParent());
			}
		}

		long byteCount = 0;
		try( Writer out = new OutputStreamWriter(getOutputStream(file, append), "UTF-8") )
		{
			byteCount = CharStreams.copy(content, out);
		}

		return new FileInfo(byteCount, filename);
	}

	@Override
	public FileInfo getFileInfo(FileHandle handle, String filename)
	{
		File extFile = getFile(handle, filename);
		return new FileInfo(extFile.length(), extFile.getName());
	}

	/*
	 * (non-Javadoc)
	 * @see com.tle.core.services.FileSystemService#saveFiles(java.lang.String,
	 * com.tle.core.filesystem.FileHandle)
	 */
	@Override
	public void saveFiles(StagingFile staging, FileHandle destination) throws IOException
	{
		ensureNotStaging(destination);

		LOGGER.info("saveFiles:" + staging.getAbsolutePath());

		File from = getFile(staging);
		File to = getFile(destination);

		File trash = null;
		if( FileSystemHelper.exists(to) )
		{
			LOGGER.debug("Destination exists renaming");
			trash = getFile(new TrashFile(staging));
			if( !FileSystemHelper.rename(to, trash, false) )
			{
				throw new FileSystemException("Couldn't move to Trash");
			}
		}

		if( !FileSystemHelper.exists(from) )
		{
			LOGGER.debug("no files to commit");
		}
		else
		{
			FileSystemHelper.copy(from, to);
		}

		if( trash != null )
		{
			FileUtils.delete(trash.toPath(), null);
		}
	}

	@Override
	public void commitFiles(TemporaryFileHandle staging, FileHandle destination)
	{
		commitFiles(staging, "", destination);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.services.FileSystemService#commitFiles(com.tle.core.filesystem
	 * .StagingFile, com.tle.core.filesystem.FileHandle)
	 */
	@Override
	public void commitFiles(TemporaryFileHandle staging, String folder, FileHandle destination)
	{
		ensureNotStaging(destination);

		File from = getFile(staging, folder);
		File to = getFile(destination);

		LOGGER.info("commitFiles: from " + from.getAbsolutePath());
		LOGGER.info("commitFiles: to " + to.getAbsolutePath());

		File trash = null;
		if( FileSystemHelper.exists(to) )
		{
			LOGGER.debug("Destination exists renaming");
			trash = getFile(new TrashFile(staging));
			LOGGER.debug("Moving current to trash");
			if( !FileSystemHelper.rename(to, trash, false) )
			{
				FileSystemHelper.rename(trash, to, true);
				throw new FileSystemException("Couldn't move to Trash.");
			}
		}

		LOGGER.debug("About to rename staging to real item");
		if( !FileSystemHelper.exists(from) )
		{
			LOGGER.debug("no files to commit - making blank dir");
			FileSystemHelper.mkdir(to);
		}
		else
		{
			LOGGER.debug("Renaming staging to real item");
			if( !FileSystemHelper.rename(from, to, false) )
			{
				if( trash != null )
				{
					FileSystemHelper.rename(trash, to, true);
				}
				throw new FileSystemException(
					"Failed to commit to staging: " + from.getAbsolutePath() + " to:" + to.getAbsolutePath());
			}
		}

		if( trash != null )
		{
			LOGGER.debug("Deleting trash");
			FileUtils.delete(trash.toPath(), null);
		}
		LOGGER.debug("Done");
	}

	@Override
	public boolean fileExists(FileHandle handle)
	{
		return fileExists(handle, null);
	}

	@Override
	public boolean fileExists(FileHandle handle, String filename)
	{
		return FileSystemHelper.exists(getFile(handle, filename));
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.services.FileSystemService#fileIsDir(com.tle.core.filesystem
	 * .FileHandle, java.lang.String)
	 */
	@Override
	public boolean fileIsDir(FileHandle handle, String filename)
	{
		return FileSystemHelper.isDir(getFile(handle, filename));
	}

	@Override
	public boolean isSameFile(FileHandle handle1, String path1, FileHandle handle2, String path2)
	{
		final File file1 = getFile(handle1, path1);
		final File file2 = getFile(handle2, path2);
		try
		{
			return Files.isSameFile(file1.toPath(), file2.toPath());
		}
		catch( IOException e )
		{
			throw Throwables.propagate(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.services.FileSystemService#mkdir(com.tle.core.filesystem
	 * .FileHandle, java.lang.String)
	 */
	@Override
	public void mkdir(FileHandle handle, String filename)
	{
		FileSystemHelper.mkdir(getFile(handle, filename));
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.services.FileSystemService#rename(com.tle.core.filesystem
	 * .FileHandle, java.lang.String, java.lang.String)
	 */
	@Override
	public boolean rename(FileHandle handle, String filename, String newname)
	{
		ensureNotBanned(newname);

		File from = getFile(handle, filename);
		File to = getFile(handle, newname);
		return FileSystemHelper.renameOnly(from, to);
	}

	@Override
	public boolean move(FileHandle handle, String filename, FileHandle newHandle, String newname)
	{
		ensureNotBanned(newname);

		File from = getFile(handle, filename);
		File to = getFile(newHandle, newname);
		return FileSystemHelper.rename(from, to, false);
	}

	@Override
	public boolean move(FileHandle handle, String filename, String newname)
	{
		return move(handle, filename, handle, newname);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.services.FileSystemService#copy(com.tle.core.filesystem.
	 * FileHandle, java.lang.String, java.lang.String)
	 */
	@Override
	public FileInfo copy(FileHandle handle, String filename, String newname)
	{
		return copy(handle, filename, handle, newname);
	}

	@Override
	public FileInfo copy(FileHandle handle, String filename, FileHandle toHandle, String newname)
	{
		File from = getFile(handle, filename);
		File to = getFile(toHandle, newname);

		if( from.isFile() )
		{
			ensureNotBanned(newname);
		}

		return new FileInfo(doCopy(from, to, false), to.getName());
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.services.FileSystemService#copy(com.tle.core.filesystem.
	 * FileHandle, java.lang.String, java.lang.String)
	 */
	@Override
	public FileInfo copy(FileHandle source, FileHandle destination)
	{
		File from = getFile(source);
		File to = getFile(destination);
		if( from.exists() )
		{
			return new FileInfo(doCopy(from, to, false), to.getName());
		}
		else
		{
			// throw new RuntimeException("Source does not exist");
			LOGGER.info("Source '" + source.getAbsolutePath() + "' does not exist");
		}
		return new FileInfo(0, to.getName());
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.services.FileSystemService#copyToStaging(com.tle.core.filesystem
	 * .FileHandle, java.lang.String, com.tle.core.filesystem.StagingFile,
	 * java.lang.String, boolean)
	 */
	@Override
	public void copyToStaging(FileHandle source, String filename, TemporaryFileHandle staging, String newname,
		boolean ignoreInternalFiles) throws IOException
	{
		File from = getFile(source, filename);
		File to = getFile(staging, newname);

		if( from.exists() )
		{
			doCopy(from, to, ignoreInternalFiles);
		}
		else
		{
			// throw new RuntimeException("Source does not exist");
			LOGGER.info(
				"Source (" + from.getAbsolutePath() + ", from original filename: " + filename + ") does not exist.");
		}
	}

	@Override
	public void copyToStaging(FileHandle handle, TemporaryFileHandle staging, boolean ignoreInteralFiles)
		throws IOException
	{
		copyToStaging(handle, "", staging, "", ignoreInteralFiles);
	}

	private long doCopy(File from, File to, boolean ignoreInternalFiles)
	{
		try
		{
			return FileSystemHelper.copy(from, to, ignoreInternalFiles, false);
		}
		catch( IOException ioe )
		{
			throw new FileSystemException("Error copying " + from + " to " + to, ioe);
		}
	}

	private InputStream getInputStream(File file) throws IOException
	{
		return new BufferedInputStream(new FileInputStream(file));
	}

	private OutputStream getOutputStream(File file, boolean append) throws IOException
	{
		file.getParentFile().mkdirs(); // NOSONAR - see mkdirs comment above
		return new BufferedOutputStream(new FileOutputStream(file, append && file.exists()));
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.services.FileSystemService#retrieveCachedFile(com.tle.core
	 * .filesystem.CachedFile, java.lang.String, long)
	 */
	@Override
	public InputStream retrieveCachedFile(FileHandle handle, String filename, long time) throws IOException
	{
		InputStream stream = null;
		if( isFileCached(handle, filename, time) )
		{
			stream = read(handle, filename);
		}
		return stream;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.services.FileSystemService#isFileCached(com.tle.core.filesystem
	 * .CachedFile, java.lang.String, long)
	 */
	@Override
	public boolean isFileCached(FileHandle handle, String filename, long time)
	{
		File file = getFile(handle, filename);
		boolean cached = false;
		if( file.exists() )
		{
			long lastModified = file.lastModified();
			long currentTime = System.currentTimeMillis();

			// If the file is still within accepted cache time
			cached = lastModified + time > currentTime;
		}
		return cached;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.services.FileSystemService#fileLength(com.tle.core.filesystem
	 * .FileHandle, java.lang.String)
	 */
	@Override
	public long fileLength(FileHandle handle, String filename) throws FileNotFoundException
	{
		return FileUtils.fileSize(getFile(handle, filename).toPath());
	}

	@Override
	public long recursivefileLength(FileHandle handle, String filename) throws IOException
	{
		return FileSystemHelper.recursiveFileLength(getFile(handle, filename));
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.services.FileSystemService#lastModified(com.tle.core.filesystem
	 * .FileHandle, java.lang.String)
	 */
	@Override
	public long lastModified(FileHandle handle, String filename)
	{
		return FileSystemHelper.lastModified(getFile(handle, filename));
	}

	@Override
	public long mostRecentModification(FileHandle handle, String dirname)
	{
		return FileSystemHelper.mostRecentModification(getFile(handle, dirname));
	}

	/**
	 * NOTE: This will close your OutputStream.
	 */
	@Override
	public void zipFile(FileHandle handle, OutputStream out, ArchiveType archiveType) throws IOException
	{
		zipFile(handle, null, out, archiveType, null);
	}

	@Override
	public void zipFile(FileHandle handle, String path, FileHandle out, String outFilename, ArchiveType archiveType,
		final ArchiveProgress progress) throws IOException
	{
		zipFile(handle, path, getOutputStream(out, outFilename, false), archiveType, progress);
	}

	@Override
	public void zipFile(FileHandle handle, String path, FileHandle out, String outFilename, ArchiveType archiveType)
		throws IOException
	{
		zipFile(handle, path, out, outFilename, archiveType, null);
	}

	/**
	 * Actual implementation NOTE: This will close your OutputStream.
	 */
	@Override
	public void zipFile(FileHandle handle, String path, OutputStream out, ArchiveType archiveType,
		final ArchiveProgress progress) throws IOException
	{
		try( ArchiveCreator archiver = archiveType.createArchiver(out) )
		{
			apply(handle, path, "**", new GrepFunctor()
			{
				@Override
				public void matched(Path file, String relFilepath)
				{
					if( Files.isDirectory(file) )
					{
						return;
					}

					try
					{
						OutputStream entry = archiver.newEntry(FileSystemHelper.decode(relFilepath), Files.size(file));
						try( InputStream in = Files.newInputStream(file, StandardOpenOption.READ) )
						{
							copyStream(in, entry, null);
						}
						finally
						{
							archiver.closeEntry();
						}

						if( progress != null )
						{
							progress.nextEntry(relFilepath);
						}
					}
					catch( IOException ex )
					{
						throw new RuntimeException("Error archiving directory", ex);
					}
				}
			});
		}
	}

	@Override
	public boolean isArchive(FileHandle handle, String zipfile)
	{
		return ArchiveType.isArchiveType(zipfile);
	}

	@Override
	public void unzipFile(FileHandle handle, InputStream in, ArchiveType method, final ArchiveProgress progress)
		throws IOException
	{
		unzipPrivate(handle, null, in, method, progress);
	}

	@Override
	public void unzipFile(FileHandle handle, InputStream in, ArchiveType method) throws IOException
	{
		unzipPrivate(handle, null, in, method, null);
	}

	@Override
	public FileInfo unzipFile(FileHandle handle, String zipfile, String outpath) throws IOException
	{
		return unzipFile(handle, zipfile, outpath, null);
	}

	private static class ZipFileArchiveExtractor extends ArchiveProgress implements ArchiveExtractor {
		private final Enumeration<? extends ZipEntry> entries;
		private final ZipFile zipFile;
		private InputStream stream;
		private volatile int upto;
		private volatile boolean finished;

		public ZipFileArchiveExtractor(ZipFile zipFile)
		{
			super(zipFile.size());
			this.zipFile = zipFile;
			entries = zipFile.entries();
		}

		@Override
		public ArchiveEntry getNextEntry() throws IOException
		{
			if (entries.hasMoreElements())
			{
				ZipEntry zipEntry = entries.nextElement();
				stream = zipFile.getInputStream(zipEntry);
				return new ArchiveEntry(zipEntry.getName().replace('\\', '/'), zipEntry.isDirectory(), zipEntry.getSize());
			}
			else return null;
		}

		@Override
		public InputStream getStream()
		{
			return stream;
		}

		@Override
		public void nextEntry(String entryPath)
		{
			upto++;
		}
	}

	@Override
	public ZipProgress unzipWithProgress(FileHandle handle, String zipPath, String targetPath) throws IOException
	{
		File zipFile = getFile(handle, zipPath);
		ZipFile zip = new ZipFile(zipFile);
		ZipFileArchiveExtractor zipFileExtractor = new ZipFileArchiveExtractor(zip);
		new Thread(() ->
		{
            try
            {
                extract(zipFileExtractor, getFile(handle, targetPath), zipFileExtractor);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
            finally
            {
				try
				{
					zip.close();
				}
				catch (IOException e)
				{

				}
				
				zipFileExtractor.finished = true;
            }
        }).start();
		return new ZipProgress()
		{
			@Override
			public int getTotalFiles()
			{
				return (int) zipFileExtractor.getEntryCount();
			}

			@Override
			public int getCurrentFile()
			{
				return zipFileExtractor.upto;
			}

			@Override
			public boolean isFinished()
			{
				return zipFileExtractor.finished;
			}
		};
	}

	@Override
	public FileInfo unzipFile(FileHandle handle, String zipfile, String outpath, final ArchiveProgress progress)
		throws IOException
	{
		return new FileInfo(0,
			unzipPrivate(handle, outpath, read(handle, zipfile), ArchiveType.getForFilename(zipfile), progress));
	}

	private String unzipPrivate(FileHandle handle, String destination, InputStream in, ArchiveType method,
		final ArchiveProgress progress) throws IOException
	{
		File outdir = getFile(handle, destination);
		if( outdir.exists() && !outdir.isDirectory() )
		{
			throw new IOException("Destination is not a directory: " + outdir);
		}

		try( InputStream in2 = in )
		{
			extract(method.createExtractor(in2), outdir, progress);
			return outdir.getName();
		}
	}

	/**
	 * @param extractor
	 * @param destination
	 * @param progress The presence of a ArchiveProgress parameter indicates a
	 *            willingness to handle failures while unzipping, if null an
	 *            exception is thrown on first IO error
	 * @throws IOException
	 */
	private void extract(ArchiveExtractor extractor, File destination, final ArchiveProgress progress)
		throws IOException
	{
		boolean madeDirs = destination.mkdirs();
		if( !madeDirs && !destination.exists() )
		{
			throw new IOException("Could not create directory " + destination.getAbsolutePath());
		}

		ArchiveEntry entry = extractor.getNextEntry();
		while( entry != null )
		{
			String target = destination.getPath() + '/' + FileSystemHelper.encode(entry.getName());
			if( entry.isDirectory() )
			{
				new File(target).mkdirs();
			}
			else
			{
				File tfile = new File(target);
				tfile.getParentFile().mkdirs();

				try( OutputStream out = new BufferedOutputStream(new FileOutputStream(tfile)) )
				{
					ByteStreams.copy(extractor.getStream(), out);
				}
				catch( IOException ex )
				{
					LOGGER.warn("Could not extract " + entry.getName(), ex);
					if( progress != null )
					{
						progress.incrementWarningCount();
						progress.setCallbackMessageValue(entry.getName());
					}
					else
					{
						throw ex;
					}
				}
			}

			if( progress != null )
			{
				progress.nextEntry(entry.getName());
			}

			entry = extractor.getNextEntry();
		}
	}

	@Override
	public ArchiveEntry findZipEntry(FileHandle handle, String filename, String entryToFind, boolean matchCase)
	{
		try( InputStream in = read(handle, filename) )
		{
			final ArchiveType method = ArchiveType.getForFilename(filename);
			final ArchiveExtractor extractor = method.createExtractor(in);
			final String lookFor = (matchCase ? entryToFind : entryToFind.toLowerCase());
			ArchiveEntry entry = extractor.getNextEntry();
			while( entry != null )
			{
				String entryName = (matchCase ? entry.getName() : entry.getName().toLowerCase());
				if( entryName.equals(lookFor) )
				{
					return entry;
				}
				entry = extractor.getNextEntry();
			}
			return null;
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
	}

	@Override
	public void extractNamedZipEntryAsStream(FileHandle handle, String packageZipName, String entryToFind,
		OutputStream out)
	{
		try( InputStream in = read(handle, packageZipName) )
		{
			final ArchiveType method = ArchiveType.getForFilename(packageZipName);
			final ArchiveExtractor extractor = method.createExtractor(in);
			ArchiveEntry matchedEntry = extractor.getNextEntry();
			while( matchedEntry != null )
			{
				String entryName = matchedEntry.getName();
				if( entryName.equals(entryToFind) )
				{
					break;
				}
				matchedEntry = extractor.getNextEntry();
			}
			if( matchedEntry != null )
			{
				try
				{
					// int copiedBytes =
					copyStream(extractor.getStream(), out, null);
				}
				catch( IOException ex )
				{
					LOGGER.warn("Could not extract " + matchedEntry.getName(), ex);
					throw ex;
				}
			}
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
	}

	@Override
	public ByteArrayOutputStream extractNamedZipEntryAsStream(FileHandle handle, String packageZipName,
		String entryToFind)
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		extractNamedZipEntryAsStream(handle, packageZipName, entryToFind, out);
		return out;
	}

	@Override
	public boolean removeFile(FileHandle handle)
	{
		return removeFile(handle, null, null);
	}

	@Override
	public boolean removeFile(FileHandle handle, String filename)
	{
		return removeFile(handle, filename, null);
	}

	@Override
	public boolean removeFile(FileHandle handle, String filename, FileCallback callback)
	{
		File target = getFile(handle, filename);
		if( LOGGER.isDebugEnabled() )
		{
			LOGGER.debug("removeFile:" + handle.getAbsolutePath() + '/' + filename);
		}
		FileUtils.FileCallback wrappedCallback = (callback == null ? null : new FileUtils.FileCallback()
		{
			@Override
			public void fileProcessed(Path file1, Path file2)
			{
				callback.fileProcessed(file1.toFile(), file2 == null ? null : file2.toFile());
			}
		});
		return FileUtils.delete(target.toPath(), wrappedCallback);
	}

	/**
	 * Indicates whether the given filename has a file extension from the given
	 * collection.
	 *
	 * @param filename a filename to check.
	 * @param fileExtensions a list of file extensions.
	 * @return true if the filename has one of the extensions in the collection.
	 */
	@Override
	public boolean hasFileExtension(String filename, Collection<String> fileExtensions)
	{
		int dotIndex = filename.lastIndexOf('.');
		if( dotIndex > -1 )
		{
			String extension = filename.substring(dotIndex + 1);

			for( String ext : fileExtensions )
			{
				if( ext.equalsIgnoreCase(extension) )
				{
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String getMD5Checksum(FileHandle handle, String path)
	{
		try
		{
			return FileSystemHelper.md5recurse(getFile(handle, path), new byte[Short.MAX_VALUE]);
		}
		catch( Exception e )
		{
			throw new FileSystemException("FATAL", e);
		}
	}

	@Override
	public List<String> grep(FileHandle handle, String path, String pattern)
	{
		Path dirs = getFile(handle, path).toPath();
		List<String> files = FileUtils.grep(dirs, pattern, true);
		return FileSystemHelper.decode(files);
	}

	@Override
	public List<String> grepIncludingDirs(FileHandle handle, String path, String pattern)
	{
		Path dirs = getFile(handle, path).toPath();
		List<String> files = FileUtils.grep(dirs, pattern, false);
		return FileSystemHelper.decode(files);
	}

	@Override
	public void apply(final FileHandle root, String path, String pattern, final GrepFunctor functor)
	{
		final File base = getFile(root, path);
		FileUtils.grep(base.toPath(), pattern, functor, true);
	}

	@Override
	public FileContentStream getContentStream(FileHandle handle, String path, String mimeType)
	{
		final File file = getFile(handle, path);
		File parent = file.getParentFile();
		while( parent != null )
		{
			if( parent.getName().toUpperCase().equals(SECURE_FOLDER) )
			{
				throw Throwables.propagate(new com.tle.exceptions.AccessDeniedException(
					CurrentLocale.get(KEY_PFX+"error.protectedresource")));
			}
			parent = parent.getParentFile();
		}
		return getContentStream(file, mimeType);
	}

	@Override
	public FileContentStream getInsecureContentStream(FileHandle handle, String path, String mimeType)
	{
		final File file = getFile(handle, path);
		return getContentStream(file, mimeType);
	}

	private FileContentStream getContentStream(File file, String mimeType)
	{
		return new FileContentStream(file, file.getName(), mimeType);
	}

	@Override
	public void checkServiceRequest(CheckServiceRequestEvent request)
	{
		ServiceStatus status = new ServiceStatus(ServiceName.FILESTORE);
		boolean isGood = true;
		List<Filestore> fstores = new ArrayList<>();
		fstores.add(new Filestore(null, "default", rootDir.toPath()));
		if( advancedFilestore )
		{
			fstores.addAll(filestores.values());
		}

		boolean first = true;
		final StringBuilder result = new StringBuilder();
		for( Filestore fs : fstores )
		{
			if( !first )
			{
				result.append("\n\n");
			}
			first = false;

			TrashFile testFile = new TrashFile(new StagingFile(FileSystemService.class.getName()));
			testFile.setFilestoreId(fs.getId());
			final File fsRoot = fs.getRoot().toFile();

			try
			{
				final String filename = "test" + UUID.randomUUID().toString() + ".txt";
				write(testFile, filename, org.apache.commons.io.IOUtils.toInputStream("test 12 12"), false);
				boolean fileExists = fileExists(testFile, filename);
				if( fileExists && removeFile(testFile, filename) )
				{
					// 1024 ^ 2 = 1048576
					result.append(CurrentLocale.get(KEY_PFX+"fileservicecheck.moreinfo",
						fsRoot.getAbsolutePath(), fsRoot.getTotalSpace() / 1048576, fsRoot.getFreeSpace() / 1048576));
				}
			}
			catch( Exception e )
			{
				LOGGER.error("Service check error", e);
				result.append(CurrentLocale.get(KEY_PFX+"fileservicecheck.error", fsRoot.getAbsolutePath(),
					e.getMessage()));
			}
		}

		status.setMoreInfo(result.toString());
		status.setServiceStatus(isGood ? Status.GOOD : Status.BAD);
		eventService.publishApplicationEvent(
			new CheckServiceResponseEvent(request.getRequetserNodeId(), zkService.getNodeId(), status));
	}

	@Override
	public List<NameValue> listFilestores()
	{
		List<NameValue> nvs = new ArrayList<>();
		//FIXME: i18n?
		nvs.add(new NameValue("default", "default"));
		if( advancedFilestore )
		{
			for( Filestore filestore : filestores.values() )
			{
				nvs.add(new NameValue(filestore.getName(), filestore.getId()));
			}
		}
		return nvs;
	}

	@Override
	public boolean isAdvancedFilestore()
	{
		return advancedFilestore;
	}
}
