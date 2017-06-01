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

package com.tle.web.scripting.advanced.types;

import java.io.Serializable;
import java.util.List;

/**
 * A MIME type object for scripts
 */
public interface MimeTypeScriptType extends Serializable
{
	/**
	 * @return The MIME type E.g. text/plain
	 */
	String getType();

	/**
	 * @return A friendly name for the MIME type
	 */
	String getDescription();

	/**
	 * @return The list of known file extensions for this MIME type
	 */
	List<String> getFileExtensions();
}
