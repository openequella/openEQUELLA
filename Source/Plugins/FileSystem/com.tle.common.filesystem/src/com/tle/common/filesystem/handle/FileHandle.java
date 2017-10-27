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

package com.tle.common.filesystem.handle;

import java.io.Serializable;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;

@NonNullByDefault
public interface FileHandle extends Serializable
{
	/**
	 * Not an absolute path in the truest sense, but if you consider the
	 * filestore folder the root of everything, then the path returned by this
	 * method is absolute.
	 * 
	 * @return The path of this handle relative to the filestore folder.
	 */
	String getAbsolutePath();

	/**
	 * Gets only the path component specified by this handle
	 * 
	 * @return
	 */
	String getMyPathComponent();

	/**
	 * Gets the ID of the filestore (or null for the default as defined by filestore.root in mandatory-config.properties).
	 * Generally only applicable to ItemFile
	 * @return The short ID of the filestore.
	 */
	@Nullable
	String getFilestoreId();
}
