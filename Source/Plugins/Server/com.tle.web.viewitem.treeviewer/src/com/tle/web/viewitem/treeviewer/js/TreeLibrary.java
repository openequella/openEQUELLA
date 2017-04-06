package com.tle.web.viewitem.treeviewer.js;

import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.js.generic.function.IncludeFile;

@SuppressWarnings("nls")
public final class TreeLibrary
{
	private static final PluginResourceHelper URL_HELPER = ResourcesService.getResourceHelper(TreeLibrary.class);

	public static final IncludeFile INCLUDE = new IncludeFile(URL_HELPER.url("scripts/treelib.js"), new IncludeFile(
		"scripts/utf8.js"));

	private TreeLibrary()
	{
		throw new Error();
	}
}
