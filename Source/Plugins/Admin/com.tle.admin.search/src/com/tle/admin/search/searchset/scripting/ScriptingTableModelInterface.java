package com.tle.admin.search.searchset.scripting;

import com.tle.beans.entity.BaseEntity;

/**
 * @author Nicholas Read
 */
public interface ScriptingTableModelInterface<T extends BaseEntity>
{
	boolean isScriptingEnabled(int row);

	T getEntity(int row);
}