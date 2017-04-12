package com.tle.core.services.http;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.tle.common.NameValue;

/**
 * To be used with HttpService
 * 
 * @author Aaron
 */
public interface Response extends Closeable
{
	/**
	 * @return true if it is any 2xx response.
	 */
	boolean isOk();

	int getCode();

	String getMessage();

	/**
	 * @return Will return null if you used the getWebContent with an
	 *         OutputStream
	 */
	String getBody();

	List<NameValue> getHeaders();

	String getHeader(String name);

	InputStream getInputStream() throws IOException;

	boolean isStreaming();

	/**
	 * Closes enclosed input stream and the supplied out
	 * 
	 * @param out
	 */
	void copy(OutputStream out);
}
