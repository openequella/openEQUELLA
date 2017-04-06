package com.tle.core.activation.workflow;

import java.util.Date;

import com.google.inject.assistedinject.Assisted;
import com.tle.core.guice.BindFactory;

@BindFactory
public interface OperationFactory
{
	RolloverOperation createRollover(long courseId, @Assisted("from") Date from, @Assisted("until") Date until);

	DeactivateOperation createDeactivate(long requestId);

	DeleteActivationOperation createDelete(long requestId);

	DeactivateOperation createDeactivate();

	DeleteActivationOperation createDelete();

	ActivateOperation createActivate(String activationType);

	ReassignActivationOperation reassignActivations(@Assisted("fromUserId") String fromUserId,
		@Assisted("toUserId") String toUserId);
}
