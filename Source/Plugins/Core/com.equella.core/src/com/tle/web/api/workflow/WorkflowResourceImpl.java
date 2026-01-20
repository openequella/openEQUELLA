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

import com.google.common.base.Strings;
import com.tle.common.i18n.CurrentLocale;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Bind(WorkflowResource.class)
@Singleton
public class WorkflowResourceImpl
    extends AbstractBaseEntityResource<Workflow, BaseEntitySecurityBean, WorkflowBean>
    implements WorkflowResource {
  private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowResourceImpl.class);

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

  /**
   * Retrieves task trend statistics across all workflows.
   *
   * @param trend The time period for trend calculation (WEEK or MONTH, case-insensitive)
   * @return HTTP 200 with JSON array of {@link TaskTrendBean} objects. Returns empty array if no
   *     tasks are waiting.
   * @throws BadRequestException (400) if trend parameter is missing or invalid
   */
  @Override
  public Response getTrends(String trend) {
    Trend trendEnum = validateTrend(trend);
    List<TaskTrend> trends = taskStatisticsService.getWaitingTasks(trendEnum);
    return buildTrendResponse(trends);
  }

  /**
   * Retrieves task trend statistics for a specific workflow.
   *
   * @param uuid The UUID of the workflow to query
   * @param trend The time period for trend calculation (WEEK or MONTH, case-insensitive)
   * @return HTTP 200 with JSON array of {@link TaskTrendBean} objects
   * @throws BadRequestException (400) if uuid or trend parameter is missing/invalid
   * @throws NotFoundException (404) if workflow with specified UUID does not exist
   */
  @Override
  public Response getTrendsForWorkflow(String uuid, String trend) {
    validateWorkflowUuid(uuid);
    Trend trendEnum = validateTrend(trend);
    List<TaskTrend> trends = taskStatisticsService.getWaitingTasksForWorkflow(uuid, trendEnum);
    return buildTrendResponse(trends);
  }

  /**
   * Helper to parse the trend string parameter into a Trend enum.
   *
   * @param trend The trend string to be parsed.
   * @return The corresponding Trend enum.
   * @throws BadRequestException If the trend string is null, empty, or invalid.
   */
  private Trend validateTrend(String trend) {
    if (Strings.isNullOrEmpty(trend)) {
      throw new BadRequestException(
          CurrentLocale.get("com.tle.web.api.workflow.error.trendmissing"));
    }

    return Arrays.stream(Trend.values())
        .filter(t -> t.name().equalsIgnoreCase(trend))
        .findFirst()
        .orElseThrow(
            () ->
                new BadRequestException(
                    CurrentLocale.get("com.tle.web.api.workflow.error.trendinvalid", trend)));
  }

  private void validateWorkflowUuid(String uuid) {
    if (Strings.isNullOrEmpty(uuid)) {
      throw new BadRequestException(
          CurrentLocale.get("com.tle.web.api.workflow.error.uuidmissing"));
    }
    try {
      if (!workflowService.existsByUuid(uuid)) {
        throw new NotFoundException(
            CurrentLocale.get("com.tle.web.api.workflow.error.workflownotfound", uuid));
      }
    } catch (IllegalArgumentException e) {
      throw new BadRequestException(e);
    }
  }

  /** Helper to batch resolve task names and build the standard JSON response. */
  private Response buildTrendResponse(List<TaskTrend> trends) {
    List<TaskTrendBean> beans = transformToTrendBeans(trends);
    return Response.ok(beans).build();
  }

  private List<TaskTrendBean> transformToTrendBeans(List<TaskTrend> trends) {
    Map<Long, String> bundleNames = resolveBundleNames(trends);
    return trends.stream().map(t -> mapToTrendBean(t, bundleNames)).collect(Collectors.toList());
  }

  private Map<Long, String> resolveBundleNames(List<TaskTrend> trends) {
    List<Long> bundleIds = trends.stream().map(TaskTrend::getNameId).collect(Collectors.toList());
    bundleCache.addBundleIds(bundleIds);
    return bundleCache.getBundleMap();
  }

  private TaskTrendBean mapToTrendBean(TaskTrend trend, Map<Long, String> bundleNames) {
    String taskId = resolveTaskIdentifier(trend);
    String taskName = resolveBundleName(trend.getNameId(), bundleNames, taskId);
    return new TaskTrendBean(taskId, taskName, trend.getWaiting(), trend.getTrend());
  }

  private String resolveTaskIdentifier(TaskTrend trend) {
    return String.valueOf(trend.getWorkflowItemId());
  }

  private String resolveBundleName(Long nameId, Map<Long, String> names, String fallback) {
    return Optional.ofNullable(names.get(nameId))
        .orElseGet(
            () -> {
              LOGGER.warn("Bundle name not found for ID: {}, using fallback: {}", nameId, fallback);
              return fallback;
            });
  }
}
