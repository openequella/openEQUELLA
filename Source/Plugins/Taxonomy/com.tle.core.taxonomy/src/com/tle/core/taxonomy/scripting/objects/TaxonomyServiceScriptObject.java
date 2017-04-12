package com.tle.core.taxonomy.scripting.objects;

import com.tle.common.scripting.ScriptObject;
import com.tle.core.taxonomy.scripting.types.TaxonomyScriptType;

/**
 * Referenced by the 'data' variable in script
 */
public interface TaxonomyServiceScriptObject extends ScriptObject
{
	String DEFAULT_VARIABLE = "data"; //$NON-NLS-1$

	/**
	 * Find a taxonomy by the UUID of the taxonomy
	 * 
	 * @param uuid The UUID of the taxonomy to find.
	 * @return Will return null if no taxonomy with the given UUID is found.
	 */
	TaxonomyScriptType getTaxonomyByUuid(String uuid);

	/**
	 * Find a taxonomy by the display name of the taxonomy (in the current
	 * user's language). If multiple taxonomies with the same name are found
	 * then a RuntimeException is thrown.
	 * 
	 * @param name The display name of the taxonomy to find.
	 * @return Will return null if no taxonomy by this name is found.
	 */
	TaxonomyScriptType getTaxonomyByName(String name);
}
