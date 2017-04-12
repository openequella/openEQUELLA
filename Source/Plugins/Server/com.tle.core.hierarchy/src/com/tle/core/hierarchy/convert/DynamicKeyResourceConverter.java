package com.tle.core.hierarchy.convert;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.Institution;
import com.tle.beans.hierarchy.HierarchyTopicDynamicKeyResources;
import com.tle.core.filesystem.BucketFile;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.filesystem.TemporaryFileHandle;
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
