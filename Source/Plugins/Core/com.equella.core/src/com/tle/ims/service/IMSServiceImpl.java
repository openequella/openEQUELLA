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

package com.tle.ims.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;

import com.dytech.common.io.UnicodeReader;
import com.dytech.edge.exceptions.FileSystemException;
import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.tle.common.Check;
import com.tle.common.PathUtils;
import com.tle.common.filesystem.FileSystemConstants;
import com.tle.common.filesystem.FileSystemHelper;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.services.FileSystemService;
import com.tle.core.services.impl.FileSystemServiceImpl;
import com.tle.core.util.archive.ArchiveType;
import com.tle.core.util.ims.IMSUtilities;
import com.tle.core.util.ims.beans.IMSManifest;
import com.tle.core.xstream.TLEXStream;

@Bind(IMSService.class)
@Singleton
@SuppressWarnings("nls")
public class IMSServiceImpl implements IMSService
{
	private static final Logger LOGGER = Logger.getLogger(FileSystemServiceImpl.class);

	@Inject
	private FileSystemService fileSystemService;

	@Override
	public void ensureIMSPackage(FileHandle handle, String imsPackageName) throws IOException
	{
		getIMSFile(handle, imsPackageName);
	}

	@Override
	public InputStream getImsManifestAsStream(FileHandle handle, String packageExtractedFolder, boolean logNotFound)
		throws IOException
	{
		File newManifest = ensureCombinedManifest(handle, packageExtractedFolder);

		if( newManifest.exists() )
		{
			return getInputStream(newManifest);
		}

		if( logNotFound )
		{
			LOGGER.info(
				"Non-existent manifest (" + newManifest.getName() + ") requested for " + handle.getAbsolutePath());
		}
		return null;
	}

	@Override
	public InputStream getMetsManifestAsStream(FileHandle handle, String packName, String manifestName,
		boolean logNotFound) throws IOException
	{
		if( fileSystemService.fileExists(handle, packName) )
		{
			final String path;
			if( fileSystemService.fileIsDir(handle, packName) )
			{
				path = PathUtils.filePath(packName, manifestName);
			}
			else
			{
				path = packName;
			}
			if( fileSystemService.fileExists(handle, path) )
			{
				return new BufferedInputStream(fileSystemService.read(handle, path));
			}
		}

		if( logNotFound )
		{
			LOGGER.info("Non-existent manifest (" + packName + ", " + manifestName + ") requested for "
				+ handle.getAbsolutePath());
		}
		return null;
	}

	private File getFile(FileHandle handle, String path)
	{
		return fileSystemService.getExternalFile(handle, path);
	}

	private TLEXStream getXstream()
	{
		return TLEXStream.instance();
	}

	@Override
	public IMSManifest getImsManifest(FileHandle handle, String packageExtractedFolder, boolean logNotFound)
	{
		File newManifest = ensureCombinedManifest(handle, packageExtractedFolder);

		if( newManifest.exists() )
		{
			try( FileInputStream finp = new FileInputStream(newManifest) )
			{
				return (IMSManifest) getXstream().fromXML(new UnicodeReader(finp, "UTF-8"), IMSManifest.class);
			}
			catch( FileNotFoundException e )
			{
				LOGGER.error(e);
				// shouldn't ever happen
			}
			catch( Exception e )
			{
				LOGGER.error(e);
				// what
			}
		}
		if( logNotFound )
		{
			LOGGER.info(
				"Non-existent manifest (" + newManifest.getName() + ") requested for " + handle.getAbsolutePath());
		}
		return null;
	}

	@Override
	public IMSManifest getImsManifest(InputStream in)
	{
		return (IMSManifest) getXstream().fromXML(new UnicodeReader(in, "UTF-8"), IMSManifest.class);
	}

	@Override
	public String getImsTitle(FileHandle handle, String packageExtractedFolder)
	{
		File newManifest = ensureCombinedManifest(handle, packageExtractedFolder);
		if( newManifest.exists() )
		{
			try( FileInputStream finp = new FileInputStream(newManifest) )
			{
				return IMSUtilities.getTitleFromManifest(new UnicodeReader(finp, "UTF-8"));
			}
			catch( Exception ex )
			{
				LOGGER.error("Error getting IMS title", ex);
			}
		}

		LOGGER.info("Non-existent manifest (" + newManifest.getName() + ") requested for " + handle.getAbsolutePath());
		throw new RuntimeApplicationException("IMS manifest is invalid, or does not exist");
	}

	@Override
	public String getScormVersion(FileHandle handle, String packageExtractedFolder)
	{
		File newManifest = ensureCombinedManifest(handle, packageExtractedFolder);
		if( newManifest.exists() )
		{
			try( FileInputStream finp = new FileInputStream(newManifest) )
			{
				return IMSUtilities.getScormVersion(new UnicodeReader(finp, "UTF-8"));
			}
			catch( Exception ex )
			{
				LOGGER.error("Error getting IMS scorm type", ex);
			}
		}

		LOGGER.info("Non-existent manifest (" + newManifest.getName() + ") requested for " + handle.getAbsolutePath());
		throw new RuntimeApplicationException("IMS manifest is invalid, or does not exist");
	}

	private File ensureCombinedManifest(FileHandle handle, String packageExtractedFolder)
	{
		String filepath = PathUtils.filePath(packageExtractedFolder, IMSUtilities.IMS_MANIFEST_COMBINED);
		// Check if combined manifest file exists.
		File newManifest = getFile(handle, filepath);
		File origManifest = getFile(handle, PathUtils.filePath(packageExtractedFolder, IMSUtilities.IMS_MANIFEST));

		// Generate the manifest if need be
		if( !newManifest.exists()
			|| (origManifest.exists() && (newManifest.lastModified() < origManifest.lastModified())) )
		{
			LOGGER.info("Creating combined manifest");

			if( origManifest.exists() )
			{
				boolean madeDirs = newManifest.getParentFile().mkdirs();
				if( !(madeDirs || newManifest.getParentFile().exists()) )
				{
					throw new FileSystemException(
						"Could not create/confirm directory " + newManifest.getParentFile().getAbsolutePath());
				}

				try( Writer imsOutput = new BufferedWriter(new FileWriter(newManifest)) )
				{
					IMSUtilities.combine(new IMSUtilities.FileManifestResolver(origManifest), imsOutput);
				}
				catch( IOException ioe )
				{
					throw new FileSystemException("Error combining/copying file", ioe);
				}
				catch( Exception ex )
				{
					// If the manifest references unknown documents or
					// something, then just copy the existing manifest.
					LOGGER.warn("Could not combine manifest - simply copying", ex);
					try
					{
						FileSystemHelper.copy(origManifest, newManifest);
					}
					catch( IOException ex2 )
					{
						throw new FileSystemException("Error copying file", ex2);
					}
				}
			}
		}
		return newManifest;
	}

	private File getIMSFile(FileHandle handle, String packagePath) throws IOException
	{
		if( Check.isEmpty(packagePath) )
		{
			throw new IllegalArgumentException("Blank package name");
		}

		// packageName already has the IMS folder part
		File zipFile = getFile(handle, packagePath);
		if( !zipFile.exists() )
		{
			// Otherwise create the zip file:
			final String packageName = PathUtils.getFilenameFromFilepath(packagePath);
			final File dir = getFile(handle, packageName);
			if( !dir.exists() || !dir.isDirectory() )
			{
				throw new FileSystemException("Cannot find package source at " + packageName);
			}

			try( OutputStream out = getOutputStream(zipFile, false) )
			{
				fileSystemService.zipFile(handle, packagePath, out, ArchiveType.ZIP, null);
			}
		}

		// Now return the zip file:
		return zipFile;
	}

	private OutputStream getOutputStream(File file, boolean append) throws IOException
	{
		boolean madeDirs = file.getParentFile().mkdirs();
		if( !(madeDirs || file.getParentFile().exists()) )
		{
			throw new IOException("Could not create/confirm directory " + file.getParentFile().getAbsolutePath());
		}
		return new BufferedOutputStream(new FileOutputStream(file, append && file.exists()));
	}

	private InputStream getInputStream(File file) throws IOException
	{
		return new BufferedInputStream(new FileInputStream(file));
	}

}
