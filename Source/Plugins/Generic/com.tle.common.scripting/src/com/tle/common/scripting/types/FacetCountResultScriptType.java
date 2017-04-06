package com.tle.common.scripting.types;


/**
 * A facet count result type for use in scripts
 */
public interface FacetCountResultScriptType
{
	String getTerm();

	int getCount();
}
