/*
 * Created on Jul 14, 2004 For "The Learning Edge"
 */
package com.tle.core.item.standard.filter.workflow;

import java.util.Map;

import com.tle.beans.item.ItemStatus;
import com.tle.core.guice.Bind;
import com.tle.core.item.operations.AbstractWorkflowOperation;
import com.tle.core.item.standard.filter.AbstractStandardOperationFilter;

/**
 * @author jmaginnis
 */
@SuppressWarnings("nls")
@Bind
public class ReviewFilter extends AbstractStandardOperationFilter
{
	@Override
	public AbstractWorkflowOperation[] createOperations()
	{
		return new AbstractWorkflowOperation[]{operationFactory.review(false), operationFactory.save()};
	}

	@Override
	public void queryValues(Map<String, Object> values)
	{
		values.put("reviewDate", getDateNow());
		values.put("status", ItemStatus.LIVE.name());
	}

	@Override
	public String getJoinClause()
	{
		return "join i.moderation as m";
	}

	@Override
	public String getWhereClause()
	{
		return "moderating = false and status = :status and m.reviewDate <= :reviewDate";
	}
}
