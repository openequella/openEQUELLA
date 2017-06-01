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

import java.util.List;

/**
 * An abstraction from a real java map for use in scripting
 */
public interface MapScriptType
{
	/**
	 * Returns the value for the specified key
	 * 
	 * @param key
	 * @return The value for the key
	 */
	Object get(Object key);

	/**
	 * Returns a list of keys for this Map
	 * 
	 * @return A List of keys in the Map
	 */
	List<Object> listKeys();

	/**
	 * Returns true if there are no key/value pairs in the Map
	 * 
	 * @return True if there are no key/values in the Map
	 */
	boolean isEmpty();
}
