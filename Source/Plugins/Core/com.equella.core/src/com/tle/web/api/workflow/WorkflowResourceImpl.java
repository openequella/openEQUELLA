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

package com.tle.web.api.workflow;

import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.workflow.Trend;
import com.tle.common.workflow.Workflow;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleCache;
import com.tle.core.workflow.TaskTrend;
import com.tle.core.workflow.service.TaskStatisticsService;
import com.tle.core.workflow.service.WorkflowService;
import com.tle.web.api.baseentity.serializer.BaseEntitySerializer;
import com.tle.web.api.entity.resource.AbstractBaseEntityResource;
import com.tle.web.api.interfaces.beans.security.BaseEntitySecurityBean;
import com.tle.web.api.workflow.interfaces.WorkflowResource;
import com.tle.web.api.workflow.interfaces.beans.TaskTrendBean;
import com.tle.web.api.workflow.interfaces.beans.WorkflowBean;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Bind(WorkflowResource.class)
@Singleton
public class WorkflowResourceImpl
    extends AbstractBaseEntityResource<Workflow, BaseEntitySecurityBean, WorkflowBean>
    implements WorkflowResource {
  @Inject private WorkflowService workflowService;
  @Inject private WorkflowBeanSerializer serializer;
  @Inject private TaskStatisticsService taskStatisticsService;
  @Inject private BundleCache bundleCache;

  @Override
  protected Node[] getAllNodes() {
    return new Node[] {Node.ALL_WORKFLOWS};
  }

  @Override
  protected BaseEntitySecurityBean createAllSecurityBean() {
    return new BaseEntitySecurityBean();
  }

  @Override
  public AbstractEntityService<?, Workflow> getEntityService() {
    return workflowService;
  }

  @Override
  protected BaseEntitySerializer<Workflow, WorkflowBean> getSerializer() {
    return serializer;
  }

  @Override
  protected Class<?> getResourceClass() {
    return WorkflowResource.class;
  }

  @Override
  public Response getTrends(UriInfo uriInfo, String trend) {
    Trend trendEnum = parseTrend(trend);
    List<TaskTrend> trends = taskStatisticsService.getWaitingTasks(trendEnum);
    return buildTrendResponse(trends);
  }

  @Override
  public Response getTrendsForWorkflow(UriInfo uriInfo, String uuid, String trend) {
    if (workflowService.getByUuid(uuid) == null) {
      throw entityNotFound(uuid);
    }
    Trend trendEnum = parseTrend(trend);
    List<TaskTrend> trends = taskStatisticsService.getWaitingTasksForWorkflow(uuid, trendEnum);
    return buildTrendResponse(trends);
  }

  /**
   * Helper to parse the trend string parameter into a Trend enum. Throws a WebApplicationException
   * (400 Bad Request) if the string is invalid or null.
   */
  private Trend parseTrend(String trend) {
    try {
      return Trend.valueOf(trend.toUpperCase());
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new WebApplicationException(
          Response.status(Response.Status.BAD_REQUEST)
              .entity("Invalid trend parameter: " + trend)
              .build());
    }
  }

  /** Helper to batch resolve task names and build the standard JSON response. */
  private Response buildTrendResponse(List<TaskTrend> trends) {
    List<Long> bundleIds = new ArrayList<>();
    for (TaskTrend t : trends) {
      bundleIds.add(t.getNameId());
    }
    bundleCache.addBundleIds(bundleIds);

    Map<Long, String> names = bundleCache.getBundleMap();

    List<TaskTrendBean> resultBeans = new ArrayList<>();
    for (TaskTrend t : trends) {
      String name = names.get(t.getNameId());
      if (name == null) {
        name = "";
      }
      resultBeans.add(
          new TaskTrendBean(
              String.valueOf(t.getWorkflowItemId()), name, t.getWaiting(), t.getTrend()));
    }

    return Response.ok(resultBeans).build();
  }
}
