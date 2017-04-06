/*
 * Created on Jul 14, 2004 For "The Learning Edge"
 */
package com.tle.core.workflow.daily;

import java.util.Map;

import com.tle.beans.item.ItemStatus;
import com.tle.core.guice.Bind;
import com.tle.core.workflow.filters.BaseFilter;
import com.tle.core.workflow.operations.AbstractWorkflowOperation;

/**
 * @author jmaginnis
 */
@SuppressWarnings("nls")
@Bind
public class ReviewFilter extends BaseFilter
{
	@Override
	public AbstractWorkflowOperation[] createOperations()
	{
		return new AbstractWorkflowOperation[]{workflowFactory.review(false), workflowFactory.save()};
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
