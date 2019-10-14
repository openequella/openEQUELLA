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

package com.tle.upgrademanager.handlers;

import com.dytech.edge.common.Constants;
import com.sun.net.httpserver.HttpExchange;
import com.tle.upgrademanager.ManagerConfig;
import com.tle.upgrademanager.Utils;
import com.tle.upgrademanager.helpers.ServiceWrapper;
import com.tle.upgrademanager.helpers.Version;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.SortedSet;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

@SuppressWarnings("nls")
public class PagesHandler extends PostDispatchHandler {
  private final StringTemplateGroup templates;
  private final ManagerConfig config;

  public PagesHandler(ManagerConfig config) {
    this.config = config;
    templates = new StringTemplateGroup("templates");
    if (Boolean.getBoolean(Utils.DEBUG_FLAG)) {
      templates.setRefreshInterval(0);
    }
  }

  @Override
  public String getDefaultActionName(HttpExchange exchange) throws IOException {
    return "main";
  }

  public void main(HttpExchange exchange) throws Exception {
    StringTemplate st = templates.getInstanceOf("templates/main");
    st.setAttribute("version", new Version(config).getDeployedVersion());
    st.setAttribute("managerversion", config.getManagerDetails().getFullVersion());
    st.setAttribute("timeout", (Boolean.getBoolean(Utils.DEBUG_FLAG) ? 9999999 : 5000));
    st.setAttribute("tab_index", getIntParameterValue(exchange, "tab_index", 0));

    HttpExchangeUtils.respondHtmlMessage(exchange, 200, st.toString());
  }

  public void ajaxstatus(HttpExchange exchange) throws Exception {
    String statusText, statusClass, buttonText, buttonAction;

    final boolean serviceStarted = new ServiceWrapper(config).status();

    // algorithm as outlined in Equella 4, requirement 9.3
    if (serviceStarted) {
      final File lock = new File(config.getConfigDir(), Constants.UPGRADE_LOCK);
      if (lock.exists()) {
        if (lock.lastModified() + config.getManagerDetails().getLoadingTimeout()
            > new Date().getTime()) {
          statusText = "Loading...";
          statusClass = "statusLoading"; // $NON-NLS-1$
          buttonText = null;
          buttonAction = null;
        } else {
          statusText = "Error starting server";
          statusClass = "statusError"; // $NON-NLS-1$
          buttonText = "Stop";
          buttonAction = "/server/stop"; // $NON-NLS-1$
        }
      } else {
        statusText = "Running";
        statusClass = "statusStarted"; // $NON-NLS-1$
        buttonText = "Stop";
        buttonAction = "/server/stop"; // $NON-NLS-1$
      }
    } else {
      statusText = "Stopped";
      statusClass = "statusStopped"; // $NON-NLS-1$
      buttonText = "Start";
      buttonAction = "/server/start"; // $NON-NLS-1$
    }

    StringTemplate st = templates.getInstanceOf("templates/ajaxstatus"); // $NON-NLS-1$
    st.setAttribute("statusText", statusText); // $NON-NLS-1$
    st.setAttribute("statusClass", statusClass); // $NON-NLS-1$
    st.setAttribute("buttonText", buttonText); // $NON-NLS-1$
    st.setAttribute("buttonAction", buttonAction); // $NON-NLS-1$
    HttpExchangeUtils.respondJSONMessage(exchange, 200, new String[] {st.toString(), buttonAction});
  }

  public void versions(HttpExchange exchange) throws Exception {
    SortedSet<WebVersion> allVersions = new Version(config).getVersions();
    WebVersion deployedVersion = new Version(config).getDeployedVersion();

    Set<WebVersion> newer = allVersions.headSet(deployedVersion);
    Set<WebVersion> older = allVersions.tailSet(deployedVersion);
    older.remove(deployedVersion);

    StringTemplate st = templates.getInstanceOf("templates/versions");
    st.setAttribute("newer", newer);
    st.setAttribute("older", older);
    st.setAttribute("current", Collections.singleton(deployedVersion));
    HttpExchangeUtils.respondHtmlMessage(exchange, 200, st.toString());
  }

  public void other(HttpExchange exchange) throws Exception {
    StringTemplate st = templates.getInstanceOf("templates/other");
    HttpExchangeUtils.respondHtmlMessage(exchange, 200, st.toString());
  }

  public void troubleshooting(HttpExchange exchange) throws IOException {
    StringTemplate st = templates.getInstanceOf("templates/troubleshoot");
    HttpExchangeUtils.respondHtmlMessage(exchange, 200, st.toString());
  }

  public void progress(HttpExchange exchange) throws IOException {
    final String URI = "/pages/progress/";
    final String ajaxId = exchange.getRequestURI().toString().substring(URI.length());

    StringTemplate st = templates.getInstanceOf("templates/progress");
    st.setAttribute("ajaxId", ajaxId);
    HttpExchangeUtils.respondHtmlMessage(exchange, 200, st.toString());
  }

  public void restartmanager(HttpExchange exchange) throws IOException {
    StringTemplate st = templates.getInstanceOf("templates/restartingmanager");
    HttpExchangeUtils.respondHtmlMessage(exchange, 200, st.toString());

    // Restart manager in a new thread after a 500ms wait to ensure that the
    // response has been sent and flushed properly.
    new Thread() {
      @Override
      public void run() {
        try {
          sleep(500);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        new ServiceWrapper(config).restartmanager();
      }
    }.start();
  }

  public static class WebVersion {
    private String displayName;
    private String semanticVersion;
    private String filename;

    public WebVersion(String displayName, String semanticVersion, String filename) {
      this.displayName = displayName;
      this.filename = filename;
      this.semanticVersion = semanticVersion;
    }

    public WebVersion() {
      // Nothing
    }

    public void setSemanticVersion(String semanticVersion) {
      this.semanticVersion = semanticVersion;
    }

    public String getSemanticVersion() {
      return semanticVersion;
    }

    public String getDisplayName() {
      return displayName;
    }

    public void setDisplayName(String displayName) {
      this.displayName = displayName;
    }

    public String getFilename() {
      return filename;
    }

    public void setFilename(String filename) {
      this.filename = filename;
    }
  }
}
