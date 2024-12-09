/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.connectors.blackboard.scheduler;

import com.tle.beans.entity.BaseEntityLabel;
import com.tle.common.Check;
import com.tle.common.connectors.entity.Connector;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.core.connectors.blackboard.BlackboardConnectorConstants;
import com.tle.core.connectors.blackboard.service.BlackboardConnectorService;
import com.tle.core.connectors.service.ConnectorRepositoryService;
import com.tle.core.connectors.service.ConnectorService;
import com.tle.core.guice.Bind;
import com.tle.core.scheduler.ScheduledTask;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Bind
@Singleton
public class SynchroniseEquellaContentTask implements ScheduledTask {
  private static final Logger LOGGER = LoggerFactory.getLogger(SynchroniseEquellaContentTask.class);

  @Inject private ConnectorService connectorService;
  @Inject private BlackboardConnectorService blackboardConnectorService;
  @Inject private ConnectorRepositoryService connectorRepoService;

  @Override
  @Transactional
  @SuppressWarnings("nls")
  public void execute() {
    LOGGER.info("Starting SynchroniseEquellaContentTask");

    // Only run the task for unique BB URLs
    final Set<String> bbInstUrls = new HashSet<String>();

    final List<BaseEntityLabel> connectors = connectorService.listAll();
    for (BaseEntityLabel conn : connectors) {
      final Connector connector = connectorService.get(conn.getId());
      if (connector.getLmsType().equals(BlackboardConnectorConstants.CONNECTOR_TYPE)) {
        final String bbUrl = connector.getServerUrl();

        if (!bbInstUrls.contains(bbUrl)) {
          bbInstUrls.add(bbUrl);

          LOGGER.info("Invoking synchronise at " + bbUrl);
          try {
            // Username will be system.
            // The connector's javascript should map "system" to
            // "Administrator"

            String username = connector.getAttribute(BlackboardConnectorConstants.SYSTEM_USERNAME);
            if (Check.isEmpty(username)) {
              username = connectorRepoService.mungeUsername(CurrentUser.getUsername(), connector);
            }
            blackboardConnectorService.synchroniseEquellaContent(connector, username);
          } catch (Exception e) {
            LOGGER.error("Error invoking synchronise at " + bbUrl, e);
          }
        } else {
          LOGGER.info(
              "Not running BB synchronise content task for BB URL "
                  + bbUrl
                  + " since it is also used by another connector");
        }
      }
    }
  }
}
