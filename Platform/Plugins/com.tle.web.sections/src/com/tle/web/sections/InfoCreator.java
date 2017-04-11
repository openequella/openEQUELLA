package com.tle.web.sections;

import com.tle.annotation.NonNullByDefault;

@NonNullByDefault
public interface InfoCreator
{
	/**
	 * Create a new {@code SectionInfo} for a given path.<br>
	 * 
	 * @param path The path to get a {@code SectionInfo} for
	 * @return The newly created {@code SectionInfo}
	 */
	SectionInfo createForward(String path);
}
