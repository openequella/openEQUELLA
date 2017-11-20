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

package com.tle.core.hierarchy.convert;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.Institution;
import com.tle.beans.hierarchy.HierarchyTopicDynamicKeyResources;
import com.tle.common.filesystem.handle.BucketFile;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.hierarchy.HierarchyDao;
import com.tle.core.institution.convert.AbstractMigratableConverter;
import com.tle.core.institution.convert.ConverterParams;

@Bind
@Singleton
public class DynamicKeyResourceConverter extends AbstractMigratableConverter<Object>
{
	public static final String KEYRESOURCES_ID = "DYNAKEYRESOURCES";
	private static final String KEY_RESOURCES_IMPORT_EXPORT_FOLDER = "dynakeyresources";

	@Inject
	private HierarchyDao hierarchyDao;

	@SuppressWarnings("nls")
	@Override
	public void doExport(TemporaryFileHandle staging, Institution institution, ConverterParams params)
		throws IOException
	{
		final SubTemporaryFile dynaKeyResourceExportFolder = new SubTemporaryFile(staging,
			KEY_RESOURCES_IMPORT_EXPORT_FOLDER);
		// write out the format details
		xmlHelper.writeExportFormatXmlFile(dynaKeyResourceExportFolder, true);

		List<HierarchyTopicDynamicKeyResources> keyResources = hierarchyDao.getAllDynamicKeyResources(institution);
		for( HierarchyTopicDynamicKeyResources key : keyResources )
		{
			initialiserService.initialise(key);
			final BucketFile bucketFolder = new BucketFile(dynaKeyResourceExportFolder, key.getId());
			xmlHelper.writeXmlFile(bucketFolder, key.getId() + ".xml", key);
		}
	}

	@Override
	public void doImport(TemporaryFileHandle staging, Institution institution, ConverterParams params)
		throws IOException
	{
		final SubTemporaryFile dynaKeyResourceImportFolder = new SubTemporaryFile(staging,
			KEY_RESOURCES_IMPORT_EXPORT_FOLDER);
		final List<String> entries = xmlHelper.getXmlFileList(dynaKeyResourceImportFolder);

		for( String entry : entries )
		{
			HierarchyTopicDynamicKeyResources keyResources = xmlHelper.readXmlFile(dynaKeyResourceImportFolder, entry);
			keyResources.setInstitution(institution);
			keyResources.setId(0);

			hierarchyDao.saveDynamicKeyResources(keyResources);
			hierarchyDao.flush();
			hierarchyDao.clear();
		}
	}

	@Override
	public void doDelete(Institution institution, ConverterParams callback)
	{
		hierarchyDao.deleteAllDynamicKeyResources(institution);
	}

	@Override
	public String getStringId()
	{
		return KEYRESOURCES_ID;
	}
}
