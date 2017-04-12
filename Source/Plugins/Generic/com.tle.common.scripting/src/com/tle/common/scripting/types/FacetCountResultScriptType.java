package com.tle.common.scripting.types;

import java.io.Serializable;

/**
 * A facet count result type for use in scripts
 */
public interface FacetCountResultScriptType extends Serializable
{
	String getTerm();

	int getCount();
}
