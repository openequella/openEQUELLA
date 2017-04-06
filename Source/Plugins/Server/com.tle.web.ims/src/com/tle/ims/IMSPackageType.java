package com.tle.ims;

import com.tle.annotation.NonNullByDefault;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@NonNullByDefault
public final class IMSPackageType
{
	private IMSPackageType()
	{
		throw new Error("No");
	}

	/**
	 * Vanilla IMS
	 */
	public static final String IMS = "IMS";
	public static final String SCORM = "SCORM";
	public static final String QTITEST = "QTITEST";
}
