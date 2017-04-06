package com.tle.core.services.external;

import com.tle.beans.filesystem.FileHandle;

public interface Office2HtmlConversionService
{
	boolean isConvertibleToHtml(String file) throws Exception;

	/**
	 * Converts a file for a given uuid.
	 * 
	 * @param uuid The UUID of the item that holds the file to convert.
	 * @param type Where the file is that the UUID is referring to, ie, staging,
	 *            attachment, etc..
	 * @param file The path of the file.
	 * @param extension The extension type of the file to convert to.
	 * @return A relative path, as returned by ConversionFile
	 */
	String convert(FileHandle itemHandle, String file, String extension) throws Exception;
}