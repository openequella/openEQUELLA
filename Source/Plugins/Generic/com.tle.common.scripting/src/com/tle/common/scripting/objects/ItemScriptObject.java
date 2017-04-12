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
