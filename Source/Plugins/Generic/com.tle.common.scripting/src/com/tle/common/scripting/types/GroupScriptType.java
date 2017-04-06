package com.tle.common.scripting.types;

import com.dytech.edge.common.valuebean.GroupBean;

/**
 * Group object for usage in scripts.
 */
public interface GroupScriptType extends GroupBean
{
	/**
	 * @return A unique, unchanging ID for the group
	 */
	@Override
	String getUniqueID();

	/**
	 * @return The name of the group
	 */
	@Override
	String getName();
}
