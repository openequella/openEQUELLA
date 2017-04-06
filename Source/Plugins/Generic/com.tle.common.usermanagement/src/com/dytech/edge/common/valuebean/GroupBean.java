/*
 * Created on Feb 17, 2005
 */
package com.dytech.edge.common.valuebean;

import java.io.Serializable;

/**
 * @author adame
 */
public interface GroupBean extends Serializable
{
	/**
	 * @return a unique, unchanging ID for the group
	 */
	String getUniqueID();

	/**
	 * @return a name for the group
	 */
	String getName();
}
