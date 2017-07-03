/*
 * Created on Oct 26, 2005
 */
package com.tle.core.entity.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tle.beans.entity.LanguageBundle;

/**
 * @author Nicholas Read
 */
public interface BaseEntityDao
{
	LanguageBundle getEntityNameForId(long id);

	Map<Long, String> getUuids(Set<Long> ids);

	List<Long> getIdsFromUuids(Set<String> uuids);
}
