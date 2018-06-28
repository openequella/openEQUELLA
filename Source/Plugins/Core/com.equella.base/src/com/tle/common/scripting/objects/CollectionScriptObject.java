package com.tle.common.scripting.objects;

import com.tle.common.scripting.ScriptObject;
import com.tle.common.scripting.types.CollectionScriptType;

import java.util.List;

/**
 * Referenced by the 'collection' variable in script
 */
public interface CollectionScriptObject extends ScriptObject
{
	String DEFAULT_VARIABLE = "collection";

	/**
	 * Return a CollectionScriptType object based on the collectionUuid.
	 *
	 * @param collectionUuid The UUID of the collection to locate
	 * @return A CollectionScriptType object or null if not found
	 */
	CollectionScriptType getFromUuid(String collectionUuid);

	/**
	 * List all non-archived collections
	 *
	 * @return A list of CollectionScriptType
	 */
	List<CollectionScriptType> listCollections();
}
