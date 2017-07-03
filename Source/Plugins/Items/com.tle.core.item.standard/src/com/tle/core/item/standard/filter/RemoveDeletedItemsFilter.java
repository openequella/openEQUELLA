/*
 * Created on Jul 14, 2004 For "The Learning Edge"
 */
package com.tle.core.item.standard.filter;

import java.util.Calendar;
import java.util.Map;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.item.ItemStatus;
import com.tle.core.item.operations.WorkflowOperation;

/**
 * @author jmaginnis
 */
public class RemoveDeletedItemsFilter extends AbstractStandardOperationFilter
{
	private final int daysOld;

	@AssistedInject
	protected RemoveDeletedItemsFilter(@Assisted int daysOld)
	{
		this.daysOld = daysOld;
	}

	@Override
	public WorkflowOperation[] createOperations()
	{
		return new WorkflowOperation[]{operationFactory.purge(false)};
	}

	@Override
	public void queryValues(Map<String, Object> values)
	{
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.add(Calendar.DAY_OF_YEAR, -daysOld);
		values.put("dateModified", cal.getTime());
		values.put("status", ItemStatus.DELETED.name());
	}

	@Override
	public String getWhereClause()
	{
		return "status = :status and dateModified <= :dateModified";
	}
}
