package com.tle.common.scripting.types;

import com.dytech.edge.common.valuebean.RoleBean;

/**
 * Role object for usage in scripts
 */
public interface RoleScriptType extends RoleBean
{
	/**
	 * @return A unique, unchanging ID for the role
	 */
	@Override
	String getUniqueID();

	/**
	 * @return The name of the role
	 */
	@Override
	String getName();
}
