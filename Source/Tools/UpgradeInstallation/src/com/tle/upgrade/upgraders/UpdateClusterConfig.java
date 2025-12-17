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

package com.tle.upgrade.upgraders;

import com.tle.upgrade.PropertyFileModifier;
import com.tle.upgrade.UpgradeDepends;
import com.tle.upgrade.UpgradeResult;
import java.io.File;
import java.util.Collections;
import java.util.List;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * Changes eventService.x = blah and userSessionService.x = blah to channelService.x = blah Always
 * uses values in the event service in preference to ones found in user session service. (It's more
 * likely to have been fixed if there were any cluster mis-configurations)
 *
 * @author aholland
 */
public class UpdateClusterConfig extends AbstractUpgrader {
  private static final String BIND_ADDRESS = "bindAddress"; // $NON-NLS-1$
  private static final String MULTICAST_ADDRESS = "multicastAddress"; // $NON-NLS-1$
  private static final String MULTICAST_PORT = "multicastPort"; // $NON-NLS-1$
  private static final String CONNECTION_STRING = "connectionString"; // $NON-NLS-1$
  private static final String DEBUG = "debug"; // $NON-NLS-1$
  private static final String CLUSTER_NODE_ID = "clusterNodeId"; // $NON-NLS-1$

  private static final String EVENT_SERVICE_PREFIX = "eventService."; // $NON-NLS-1$
  private static final String USER_SESSION_SERVICE_PREFIX = "userSessionService."; // $NON-NLS-1$
  private static final String CHANNEL_SERVICE_PREFIX = "channelService."; // $NON-NLS-1$

  @Override
  public String getId() {
    return "UpdateClusterConfig"; //$NON-NLS-1$
  }

  @Override
  public List<UpgradeDepends> getDepends() {
    return Collections.emptyList();
  }

  @Override
  public boolean canBeRemoved() {
    return false;
  }

  @Override
  public void upgrade(UpgradeResult result, File tleInstallDir) throws Exception {
    new ClusterPropertyModifier(tleInstallDir).updateProperties();
  }

  public static class ClusterPropertyModifier extends PropertyFileModifier {
    protected ClusterPropertyModifier(File installDir) throws ConfigurationException {
      super(new File(new File(installDir, CONFIG_FOLDER), OPTIONAL_CONFIG));
    }

    @Override
    protected boolean modifyProperties(PropertiesConfiguration props) {
      boolean changed = false;
      changed |= changeKey(props, BIND_ADDRESS);
      changed |= changeKey(props, MULTICAST_ADDRESS);
      changed |= changeKey(props, MULTICAST_PORT);
      changed |= changeKey(props, CONNECTION_STRING, true);
      changed |= changeKey(props, DEBUG);
      changed |= changeKey(props, CLUSTER_NODE_ID);
      return changed;
    }

    private boolean changeKey(PropertiesConfiguration props, String suffix) {
      return changeKey(props, suffix, false);
    }

    private boolean changeKey(PropertiesConfiguration props, String suffix, boolean clearOnly) {
      String eventServiceVal = props.getString(EVENT_SERVICE_PREFIX + suffix);
      String sessionServiceVal = props.getString(USER_SESSION_SERVICE_PREFIX + suffix);

      String useVal = (eventServiceVal == null ? sessionServiceVal : eventServiceVal);
      if (useVal != null) {
        if (!clearOnly) {
          props.setProperty(CHANNEL_SERVICE_PREFIX + suffix, useVal);
        }
        props.clearProperty(EVENT_SERVICE_PREFIX + suffix);
        props.clearProperty(USER_SESSION_SERVICE_PREFIX + suffix);
        return true;
      }
      return false;
    }
  }
}
