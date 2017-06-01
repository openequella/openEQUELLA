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

package com.tle.common.scripting.types;

/**
 * @author Aaron
 */
public interface FileHandleScriptType
{
	/**
	 * @return Just the name of the file.
	 */
	String getName();

	/**
	 * @return The path (including the filename) of the file, relative to the
	 *         item.
	 */
	String getFilepath();

	/**
	 * @return Same as getFilepath()
	 */
	@Override
	String toString();

	/**
	 * @return File handles for other items will always be read-only. Attempting
	 *         use write/delete operations on these handles will throw an
	 *         exception.
	 */
	boolean isReadOnly();
}
