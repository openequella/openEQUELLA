package com.tle.web.stream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface ContentStream
{
	InputStream getInputStream() throws IOException;

	File getDirectFile();

	long getLastModified();

	/**
	 * This can return -1 if unknown, and can also change after
	 * {@link #getInputStream()} has been called.
	 * 
	 * @return
	 */
	long getContentLength();

	/**
	 * If {@link #getContentLength()} returns -1 the you may be able to use this
	 * to get a rough estimate. E.g. a viewer may return the raw file length
	 * without the viewer's decorations.
	 * 
	 * @return -1 if no estimate available
	 */
	long getEstimatedContentLength();

	String getMimeType();

	String getContentDisposition();

	boolean exists();

	String getFilenameWithoutPath();

	String getCacheControl();

	void setCacheControl(String cacheControl);

	boolean mustWrite();

	void write(OutputStream out) throws IOException;

}
