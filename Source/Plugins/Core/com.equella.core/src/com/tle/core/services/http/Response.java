/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
