package com.tle.core.dao;

import java.util.List;
import java.util.Map;

import com.tle.beans.security.AccessExpression;
import com.tle.common.Triple;
import com.tle.core.hibernate.dao.GenericDao;

/**
 * @author Nicholas Read
 */
public interface AccessExpressionDao extends GenericDao<AccessExpression, Long>
{
	AccessExpression retrieveOrCreate(String expression);

	void deleteOrphanedExpressions();

	List<Triple<Long, String, Boolean>> getMatchingExpressions(List<String> values);

	List<AccessExpression> listAll();

	Map<Long, Long> userIdChanged(String fromUserId, String toUserId);

	Map<Long, Long> groupIdChanged(String fromGroupId, String toGroupId);
}
