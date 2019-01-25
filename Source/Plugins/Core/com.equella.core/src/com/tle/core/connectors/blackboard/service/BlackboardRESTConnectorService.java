package com.tle.core.connectors.blackboard.service;

import com.tle.common.connectors.ConnectorCourse;
import com.tle.common.connectors.entity.Connector;
import com.tle.core.connectors.service.ConnectorRepositoryImplementation;

public interface BlackboardRESTConnectorService extends ConnectorRepositoryImplementation
{
	/**
	 *
	 * @return A sparsely populated course
	 */
	ConnectorCourse getCourse(Connector connector, String courseId);
}
