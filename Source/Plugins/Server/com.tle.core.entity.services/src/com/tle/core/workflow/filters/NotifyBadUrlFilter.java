package com.tle.core.workflow.filters;

import java.util.Map;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.ReferencedURL;
import com.tle.core.workflow.operations.WorkflowOperation;

@SuppressWarnings("nls")
public class NotifyBadUrlFilter extends BaseFilter
{
	private ReferencedURL rurl;

	@AssistedInject
	public NotifyBadUrlFilter(@Assisted ReferencedURL rurl)
	{
		this.rurl = rurl;
	}

	@Override
	protected WorkflowOperation[] createOperations()
	{
		return new WorkflowOperation[]{workflowFactory.notifyBadUrl(), workflowFactory.reIndexIfRequired(),};
	}

	@Override
	public void queryValues(Map<String, Object> values)
	{
		values.put("rurl", rurl);
	}

	@Override
	public String getWhereClause()
	{
		return "i.status IN ('LIVE', 'REVIEW', 'ARCHIVED') AND :rurl IN ELEMENTS(i.referencedUrls)";
	}

}
