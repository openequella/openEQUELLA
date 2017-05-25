package com.tle.web.filemanager;

import com.tle.web.resources.ResourcesService;

@SuppressWarnings("nls")
public final class FileManagerConstants
{
	public static final String FILEMANAGER_APPLET_JAR_URL = ResourcesService.getResourceHelper(
		FileManagerConstants.class).plugUrl("com.tle.web.filemanager.applet", "filemanager.jar");

	private FileManagerConstants()
	{
		throw new Error();
	}
}
