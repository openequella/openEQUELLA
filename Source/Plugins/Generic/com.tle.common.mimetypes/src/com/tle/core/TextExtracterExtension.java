package com.tle.core;

import java.io.IOException;
import java.io.InputStream;

import com.tle.beans.mime.MimeEntry;

/**
 * @author aholland
 */
public interface TextExtracterExtension
{
	void extractText(String mimeType, InputStream input, StringBuilder outputText, int maxSize) throws IOException;

	void setEnabledForMimeEntry(MimeEntry mimeType, boolean enabled);

	boolean isEnabledForMimeEntry(MimeEntry mimeEntry);

	boolean isMimeTypeSupported(String mimeType);

	String getNameKey();
}