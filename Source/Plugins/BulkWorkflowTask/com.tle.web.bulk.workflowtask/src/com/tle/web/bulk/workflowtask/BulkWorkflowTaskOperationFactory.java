package com.tle.web.bulk.workflowtask;

import com.google.inject.assistedinject.Assisted;
import com.tle.core.guice.BindFactory;
import com.tle.web.bulk.workflowtask.operations.TaskApproveOperation;
import com.tle.web.bulk.workflowtask.operations.TaskReassignModeratorOperation;
import com.tle.web.bulk.workflowtask.operations.TaskRejectOperation;

@BindFactory
public interface BulkWorkflowTaskOperationFactory
{
	TaskApproveOperation approve(@Assisted("message") String message,
		@Assisted("acceptAllUsers") boolean acceptAllUsers);

	TaskRejectOperation reject(@Assisted("message") String message);

	TaskReassignModeratorOperation changeModeratorAssign(@Assisted("toUser") String toUser);
}
