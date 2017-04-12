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
