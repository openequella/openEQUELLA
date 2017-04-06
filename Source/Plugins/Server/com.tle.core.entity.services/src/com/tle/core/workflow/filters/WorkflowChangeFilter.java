/*
 * Created on Aug 4, 2004
 */
package com.tle.core.workflow.filters;

import java.util.Map;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.core.workflow.operations.AbstractWorkflowOperation;

/**
 * @author jmaginnis
 */
// Sonar maintains that 'Class cannot be instantiated and does not provide any
// static methods or fields', but methinks thats bunkum
@SuppressWarnings("nls")
public final class WorkflowChangeFilter extends BaseFilter // NOSONAR
{
	private final long collectionId;

	@AssistedInject
	private WorkflowChangeFilter(@Assisted long collectionId)
	{
		this.collectionId = collectionId;
	}

	@Override
	public AbstractWorkflowOperation[] createOperations()
	{
		// Need save operation to reindex items that are changed
		return new AbstractWorkflowOperation[]{workflowFactory.reset(), workflowFactory.saveUnlock(false)};
	}

	@Override
	public void queryValues(Map<String, Object> values)
	{
		values.put("collectionId", collectionId);
	}

	@Override
	public String getWhereClause()
	{
		return "moderating = true and itemDefinition.id = :collectionId";
	}
}
