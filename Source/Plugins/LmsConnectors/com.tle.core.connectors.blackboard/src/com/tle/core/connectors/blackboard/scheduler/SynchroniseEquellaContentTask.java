package com.tle.core.connectors.blackboard.scheduler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import com.tle.beans.entity.BaseEntityLabel;
import com.tle.common.Check;
import com.tle.common.connectors.entity.Connector;
import com.tle.core.connectors.blackboard.BlackboardConnectorConstants;
import com.tle.core.connectors.blackboard.service.BlackboardConnectorService;
import com.tle.core.connectors.service.ConnectorRepositoryService;
import com.tle.core.connectors.service.ConnectorService;
import com.tle.core.guice.Bind;
import com.tle.core.scheduler.ScheduledTask;
import com.tle.core.user.CurrentUser;

/**
 * @author Aaron
 */
@Bind
@Singleton
public class SynchroniseEquellaContentTask implements ScheduledTask
{
	private static final Logger LOGGER = Logger.getLogger(SynchroniseEquellaContentTask.class);

	@Inject
	private ConnectorService connectorService;
	@Inject
	private BlackboardConnectorService blackboardConnectorService;
	@Inject
	private ConnectorRepositoryService connectorRepoService;

	@Override
	@Transactional
	@SuppressWarnings("nls")
	public void execute()
	{
		LOGGER.info("Starting SynchroniseEquellaContentTask");

		// Only run the task for unique BB URLs
		final Set<String> bbInstUrls = new HashSet<String>();

		final List<BaseEntityLabel> connectors = connectorService.listAll();
		for( BaseEntityLabel conn : connectors )
		{
			final Connector connector = connectorService.get(conn.getId());
			if( connector.getLmsType().equals(BlackboardConnectorConstants.CONNECTOR_TYPE) )
			{
				final String bbUrl = connector.getServerUrl();

				if( !bbInstUrls.contains(bbUrl) )
				{
					bbInstUrls.add(bbUrl);

					LOGGER.info("Invoking synchronise at " + bbUrl);
					try
					{
						// Username will be system.
						// The connector's javascript should map "system" to
						// "Administrator"

						String username = connector.getAttribute(BlackboardConnectorConstants.SYSTEM_USERNAME);
						if( Check.isEmpty(username) )
						{
							username = connectorRepoService.mungeUsername(CurrentUser.getUsername(), connector);
						}
						blackboardConnectorService.synchroniseEquellaContent(connector, username);
					}
					catch( Exception e )
					{
						LOGGER.error("Error invoking synchronise at " + bbUrl, e);
					}
				}
				else
				{
					LOGGER.info("Not running BB synchronise content task for BB URL " + bbUrl
						+ " since it is also used by another connector");
				}
			}
		}
	}
}
