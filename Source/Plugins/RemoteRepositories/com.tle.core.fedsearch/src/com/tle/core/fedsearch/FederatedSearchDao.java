/*
 * Created on Oct 26, 2005
 */
package com.tle.core.fedsearch;

import java.util.List;

import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.FederatedSearch;
import com.tle.core.dao.AbstractEntityDao;

/**
 * @author Nicholas Read
 */
public interface FederatedSearchDao extends AbstractEntityDao<FederatedSearch>
{
	List<Long> findEngineNamesByType(String type);

	List<FederatedSearch> enumerateAllZ3950();

	List<BaseEntityLabel> listEnabled();
}
