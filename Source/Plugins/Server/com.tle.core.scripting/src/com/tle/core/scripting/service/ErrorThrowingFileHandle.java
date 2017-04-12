package com.tle.core.scripting.service;

import com.tle.beans.filesystem.FileHandle;
import com.tle.common.i18n.CurrentLocale;

/**
 * When the staging file is not available, this object is injected in instead so
 * that a descriptive error is thrown that tells the user to use if
 * (staging.isAvailable()) { blah } in their scripts
 * 
 * @author aholland
 */
@SuppressWarnings("nls")
public class ErrorThrowingFileHandle implements FileHandle
{
	private static final long serialVersionUID = 1L;

	@Override
	public String getAbsolutePath()
	{
		throw new RuntimeException(CurrentLocale.get("com.tle.core.scripting.error.nostaging"));
	}

	@Override
	public String getMyPathComponent()
	{
		throw new RuntimeException(CurrentLocale.get("com.tle.core.scripting.error.nostaging"));
	}
}