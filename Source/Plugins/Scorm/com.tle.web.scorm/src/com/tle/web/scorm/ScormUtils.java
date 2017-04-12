package com.tle.web.scorm;

public final class ScormUtils
{
	public static final String ATTACHMENT_TYPE = "scorm";
	public static final String ATTACHMENT_RESOURCE_TYPE = "scormres";
	public static final String MIME_TYPE = "equella/scorm-package";

	/**
	 * @param instanceVersion cannot be null
	 * @return true if string starts with 1.0, 1.1, 1.2, otherwise false
	 */
	public static boolean isPreSCORM2004(String instanceVersion)
	{
		return instanceVersion.startsWith("1.0") || instanceVersion.startsWith("1.1")
			|| instanceVersion.startsWith("1.2");
	}

	private ScormUtils()
	{
		throw new Error();
	}
}
