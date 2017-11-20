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

package com.tle.common.scripting.objects;

import com.tle.common.scripting.ScriptObject;
import com.tle.common.scripting.types.ItemScriptType;

/**
 * Referenced by the 'items' variable in script
 * 
 * @author aholland
 */
public interface ItemScriptObject extends ScriptObject
{
	String DEFAULT_VARIABLE = "items"; //$NON-NLS-1$

	/**
	 * Gets an item with the supplied uuid and version, or returns null if no
	 * item with the supplied uuid and version can be found.
	 * 
	 * @param uuid The UUID of the item to get
	 * @param version The version of the item to get
	 * @return The item object or null if not found.
	 */
	ItemScriptType getItem(String uuid, int version);

	/**
	 * Gets the latest version of the item (not necessarily the Live version)
	 * with the supplied uuid, or null if no item with supplied uuid can be
	 * found.
	 * 
	 * @param uuid The UUID of the item to get
	 * @return The item object or null if not found.
	 */
	ItemScriptType getLatestVersionItem(String uuid);

	/**
	 * Gets the Live version of the item with the supplied uuid, or null if no
	 * item with supplied uuid can be found.
	 * 
	 * @param uuid The UUID of the item to get the Live version of
	 * @return The item object or null if not found.
	 */
	ItemScriptType getLiveItem(String uuid);
}
