package com.tle.core.services;

import java.util.List;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.ScriptContext;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.filesystem.FileHandle;

public interface MetadataMappingService
{

	void mapPackage(ItemDefinition collection, FileHandle handle, String packageName, PropBagEx itemxml);

	void mapHtmlTags(ItemDefinition collection, FileHandle handle, List<String> filenames, PropBagEx itemxml);

	void mapLiterals(ItemDefinition collection, PropBagEx itemxml, ScriptContext scriptContext);
}
