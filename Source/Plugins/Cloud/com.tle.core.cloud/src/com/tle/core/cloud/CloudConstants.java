package com.tle.core.cloud;

import com.tle.annotation.NonNullByDefault;

@SuppressWarnings("nls")
@NonNullByDefault
public final class CloudConstants
{
	public static final String LANGUAGE_PATH = "/oer/dc/language";
	public static final String LICENCE_PATH = "/oer/eq/license_type";
	public static final String PUBLISHER_PATH = "/oer/dc/publisher";
	public static final String EDUCATION_LEVEL_PATH = "/oer/dc/terms/educationLevel";
	public static final String FORMAT_PATH = "/oer/dc/format";

	public static final String ITEM_EXTENSION = "cloud";

	public CloudConstants()
	{
		throw new Error();
	}
}
