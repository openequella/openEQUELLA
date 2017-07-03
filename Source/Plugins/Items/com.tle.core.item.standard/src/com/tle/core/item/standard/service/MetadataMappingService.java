package com.tle.core.item.standard.service;

import java.util.List;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.ScriptContext;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.filesystem.handle.FileHandle;

public interface MetadataMappingService
{

	void mapPackage(ItemDefinition collection, FileHandle handle, String packageName, PropBagEx itemxml);

	void mapHtmlTags(ItemDefinition collection, FileHandle handle, List<String> filenames, PropBagEx itemxml);

	void mapLiterals(ItemDefinition collection, PropBagEx itemxml, ScriptContext scriptContext);
}
