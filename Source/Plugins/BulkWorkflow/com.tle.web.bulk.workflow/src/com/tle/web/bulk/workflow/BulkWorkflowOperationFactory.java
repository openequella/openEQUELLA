package com.tle.web.bulk.workflow;

import com.google.inject.assistedinject.Assisted;
import com.tle.core.guice.BindFactory;
import com.tle.web.bulk.workflow.operations.WorkflowMoveOperation;
import com.tle.web.bulk.workflow.operations.WorkflowRemoveOperation;

@BindFactory
public interface BulkWorkflowOperationFactory
{
	WorkflowRemoveOperation createRemove();

	WorkflowMoveOperation taskMove(@Assisted("msg") String msg, @Assisted("toStep") String toStep);
}
