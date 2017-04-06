package com.tle.core.qti.dao;

import java.util.Iterator;

import com.tle.common.qti.entity.QtiItemResult;
import com.tle.core.hibernate.dao.GenericDao;

/**
 * @author Aaron
 */
public interface QtiItemResultDao extends GenericDao<QtiItemResult, Long>
{
	Iterator<QtiItemResult> getIterator();

	void deleteAll();
}
