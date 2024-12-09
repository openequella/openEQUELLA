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

package com.tle.web.template.section;

import com.dytech.edge.common.Version;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.core.services.ApplicationVersion;
import com.tle.core.zookeeper.ZookeeperService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.template.Decorations;
import java.text.MessageFormat;
import javax.inject.Inject;

public class FooterSection extends AbstractPrototypeSection<FooterSection.FooterModel>
    implements HtmlRenderer {
  @ViewFactory private FreemarkerFactory viewFactory;
  @Inject private ZookeeperService zooKeeperService;

  @Override
  public String getDefaultPropertyName() {
    return "footer"; //$NON-NLS-1$
  }

  @Override
  public Class<FooterModel> getModelClass() {
    return FooterModel.class;
  }

  @Override
  @SuppressWarnings("nls")
  public SectionResult renderHtml(RenderEventContext context) {
    if (!Decorations.getDecorations(context).isFooter()) {
      return null;
    }

    final FooterModel model = getModel(context);

    boolean guest = CurrentUser.isGuest();
    if (!guest) {
      Version version = ApplicationVersion.get();
      model.setDisplayVersion(version.getDisplay());
      model.setFullVersion(
          MessageFormat.format("{0} {1}", version.getSemanticVersion(), version.getCommit()));
    }
    model.setDisplayLinks(!guest);

    if (zooKeeperService.isClusterDebugging()) {
      model.setDisplayClusterInfo(true);
      String nodeId = zooKeeperService.getNodeId();
      model.setClusterNode(nodeId.substring(0, nodeId.indexOf("-")));

      model.setClusterMembers(
          Joiner.on(", ")
              .join(
                  Collections2.transform(
                      zooKeeperService.getAppServers(),
                      new Function<String, String>() {
                        @Override
                        public String apply(String nodeId) {
                          return nodeId.substring(0, nodeId.indexOf("-"));
                        }
                      })));
    }
    // set a flag to determine if we need to tweak the credits link to
    // operate as a normal link or as a POST parameter to institutions.do
    model.setWithinInstitution(CurrentInstitution.get() != null);

    return viewFactory.createNamedResult("footer", "footer.ftl", context);
  }

  public static class FooterModel {
    private String displayVersion;
    private String fullVersion;

    private boolean displayClusterInfo;
    private boolean withinInstitution;
    private boolean displayLinks;
    private String clusterNode;
    private String clusterMembers;
    private boolean taskLeader;

    public String getClusterNode() {
      return clusterNode;
    }

    public void setClusterNode(String clusterNode) {
      this.clusterNode = clusterNode;
    }

    public boolean isDisplayClusterInfo() {
      return displayClusterInfo;
    }

    public void setDisplayClusterInfo(boolean display) {
      this.displayClusterInfo = display;
    }

    public boolean isWithinInstitution() {
      return withinInstitution;
    }

    public void setWithinInstitution(boolean withinInstitution) {
      this.withinInstitution = withinInstitution;
    }

    public String getClusterMembers() {
      return clusterMembers;
    }

    public void setClusterMembers(String clusterMembers) {
      this.clusterMembers = clusterMembers;
    }

    public String getDisplayVersion() {
      return displayVersion;
    }

    public void setDisplayVersion(String displayVersion) {
      this.displayVersion = displayVersion;
    }

    public String getFullVersion() {
      return fullVersion;
    }

    public void setFullVersion(String fullVersion) {
      this.fullVersion = fullVersion;
    }

    public boolean isDisplayLinks() {
      return displayLinks;
    }

    public void setDisplayLinks(boolean displayLinks) {
      this.displayLinks = displayLinks;
    }
  }
}
