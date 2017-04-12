package com.dytech.edge.ejb.helpers.metadata.mappers;

import java.util.List;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.filesystem.FileHandle;

/**
 * @author aholland
 */
public interface PackageMapper
{
	boolean isSupportedPackage(FileHandle handle, String packageExtractedFolder);

	void mapMetadata(ItemDefinition itemdef, PropBagEx item, FileHandle handle, String packageExtractedFolder);

	List<String> getSupportedFormatsForDisplay();
}
