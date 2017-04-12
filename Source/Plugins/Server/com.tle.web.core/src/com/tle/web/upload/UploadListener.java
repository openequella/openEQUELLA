package com.tle.web.upload;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Aaron
 */
public interface UploadListener
{
	void copyStream(InputStream in, OutputStream out);
}
