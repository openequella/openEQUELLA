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

package com.tle.core.office2html.service;

import com.tle.common.filesystem.handle.FileHandle;

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