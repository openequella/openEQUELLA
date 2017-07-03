package com.tle.core.item.standard.filter;

import java.util.Map;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.ReferencedURL;
import com.tle.core.item.operations.WorkflowOperation;

@SuppressWarnings("nls")
public class NotifyBadUrlFilter extends AbstractStandardOperationFilter
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
		return new WorkflowOperation[]{operationFactory.notifyBadUrl(), operationFactory.reIndexIfRequired(),};
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
