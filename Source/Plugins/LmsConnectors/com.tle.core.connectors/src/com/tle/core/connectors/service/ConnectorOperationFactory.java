package com.tle.core.connectors.service;

import com.google.inject.assistedinject.Assisted;
import com.tle.core.guice.BindFactory;

@BindFactory
public interface ConnectorOperationFactory
{
	RemoveContentOperation createDelete();

	MoveContentOperation createMove(@Assisted("courseId") String courseId, @Assisted("locationId") String locationId);
}
