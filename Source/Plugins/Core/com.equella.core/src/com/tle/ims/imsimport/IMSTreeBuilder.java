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

package com.tle.ims.imsimport;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.web.wizard.PackageInfo;
import org.apache.log4j.Logger;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.common.Check;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.services.FileSystemService;
import com.tle.core.util.archive.ArchiveEntry;
import com.tle.core.util.ims.IMSNavigationHelper;
import com.tle.core.util.ims.IMSUtilities;
import com.tle.core.util.ims.beans.IMSManifest;
import com.tle.core.util.ims.beans.IMSResource;
import com.tle.core.util.ims.extension.IMSFileExporter;
import com.tle.ims.IMSPackageType;
import com.tle.ims.service.IMSService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.wizard.PackageTreeBuilder;

@SuppressWarnings("nls")
@Bind
@Singleton
public class IMSTreeBuilder implements PackageTreeBuilder
{
	private static final Logger LOGGER = Logger.getLogger(IMSTreeBuilder.class);

	@Inject
	private FileSystemService fileSystem;
	@Inject
	private IMSService imsService;
	@Inject
	private IMSNavigationHelper navHelper;

	private PluginTracker<IMSFileExporter> fileImporters;

	@Override
	public PackageInfo createTree(Item item, FileHandle staging, String packageExtractedFolder,
								  String originalPackagePath, String packageName, boolean expand)
	{
		LOGGER.debug("Start IMS tree builder");

		PackageInfo result = new PackageInfo();
		result.setValid(false);

		try
		{
			IMSManifest imsManifest = imsService.getImsManifest(staging, packageExtractedFolder, true);
			if( imsManifest != null )
			{
				String scormVersion = imsService.getScormVersion(staging, packageName);

				StagingFile actualStaging = (StagingFile) staging;

				Collection<Attachment> createdAttachments = navHelper.createTree(imsManifest, item, actualStaging,
					packageName, !Check.isEmpty(scormVersion), expand);
				for( IMSFileExporter fileImporter : fileImporters.getBeanList() )
				{
					fileImporter.importFiles(item, actualStaging, packageExtractedFolder, packageName,
						createdAttachments);
				}

				result.setCreatedAttachments(createdAttachments);
				result.setTitle(imsService.getImsTitle(staging, packageName));

				result.setScormVersion(scormVersion);
				result.setValid(true);
			}
		}
		catch( IOException e )
		{
			result.setError(CurrentLocale.get("com.tle.ims.imsimport.notloaded"));
		}

		LOGGER.debug("Finished IMS tree builder");
		return result;
	}

	@Override
	public PackageInfo getInfo(SectionInfo info, FileHandle staging, String packageExtractedFolder)
	{
		PackageInfo result = new PackageInfo();
		result.setValid(false);
		try
		{
			IMSManifest imsManifest = imsService.getImsManifest(staging, packageExtractedFolder, true);
			if( imsManifest != null )
			{
				result.setTitle(imsService.getImsTitle(staging, packageExtractedFolder));
				result.setScormVersion(imsService.getScormVersion(staging, packageExtractedFolder));
				result.setValid(true);
			}
		}
		catch( Exception e )
		{
			result.setError(e.getMessage());
		}
		return result;
	}

	@Override
	public boolean canHandle(SectionInfo info, FileHandle staging, String packageFilepath)
	{
		try
		{
			ArchiveEntry entry = fileSystem.findZipEntry(staging, packageFilepath, IMSUtilities.IMS_MANIFEST, false);
			return (entry != null);
		}
		catch( Exception e )
		{
			return false;
		}
	}

	// TODO: pluginerise (QTI)
	@Override
	public List<String> determinePackageTypes(SectionInfo info, FileHandle staging, String packageFilepath)
	{
		final ArchiveEntry entry = fileSystem.findZipEntry(staging, packageFilepath, IMSUtilities.IMS_MANIFEST, false);
		if( entry != null )
		{
			// TODO: tried to use piped input and output buffers but requires
			// multi threads and you can't reset. Would be nice though so we
			// don't have to read the whole file.
			try (ByteArrayOutputStream out = new ByteArrayOutputStream())
			{
				fileSystem.extractNamedZipEntryAsStream(staging, packageFilepath, entry.getName(), out);
				try (BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(out.toByteArray())))
				{
					in.mark(Integer.MAX_VALUE);
					final String scormVer = IMSUtilities.getScormVersionFromStream(in);
					if( scormVer != null )
					{
						return Lists.newArrayList(IMSPackageType.SCORM, IMSPackageType.IMS);
					}
					in.reset();

					final IMSManifest imsManifest = imsService.getImsManifest(in);
					final List<IMSResource> allResources = imsManifest.getAllResources();
					for( IMSResource imsResource : allResources )
					{
						final String resType = imsResource.getType();
						// imsqti_xml is QTI 1.x
						if( resType != null && (resType.startsWith("imsqti_test_xml")) )
						{
							return Lists.newArrayList(IMSPackageType.QTITEST, IMSPackageType.IMS);
						}
					}
					return Lists.newArrayList(IMSPackageType.IMS);
				}
			}
			catch( Exception e )
			{
				throw Throwables.propagate(e);
			}
		}
		throw new RuntimeException("Not a package type!");
	}

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		fileImporters = new PluginTracker<IMSFileExporter>(pluginService, "com.tle.web.ims", "imsFileExporter", "id");
		fileImporters.setBeanKey("class");
	}
}
