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

package com.tle.core.activation.service.impl;

import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemActivationId;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemIdKey;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.common.Check;
import com.tle.common.beans.exception.NotFoundException;
import com.tle.common.i18n.CurrentTimeZone;
import com.tle.common.settings.standard.CourseDefaultsSettings;
import com.tle.common.util.DateHelper;
import com.tle.common.util.LocalDate;
import com.tle.common.util.UtcDate;
import com.tle.core.activation.ActivateRequestDao;
import com.tle.core.activation.ActivationConstants;
import com.tle.core.activation.service.ActivationImplementation;
import com.tle.core.activation.service.ActivationService;
import com.tle.core.activation.workflow.ActivateOperation;
import com.tle.core.activation.workflow.OperationFactory;
import com.tle.core.activation.workflow.ReassignActivationOperation;
import com.tle.core.events.UserDeletedEvent;
import com.tle.core.events.UserEditEvent;
import com.tle.core.events.UserIdChangedEvent;
import com.tle.core.events.listeners.UserChangeListener;
import com.tle.core.guice.Bind;
import com.tle.core.item.ItemIdExtension;
import com.tle.core.item.dao.ItemDao;
import com.tle.core.item.operations.ItemOperationParams;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.service.ItemService;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.security.impl.SecureOnCall;
import com.tle.core.security.impl.SecureOnReturn;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings("nls")
@Bind(ActivationService.class)
@Singleton
public class ActivationServiceImpl
    implements ActivationService, ItemIdExtension, UserChangeListener {
  private static PluginResourceHelper r =
      ResourcesService.getResourceHelper(ActivationServiceImpl.class);
  @Inject private ItemService itemService;
  @Inject private ItemDao itemDao;
  @Inject private ActivateRequestDao requestDao;
  @Inject private ConfigurationService systemConstantsService;
  @Inject private OperationFactory operationFactory;
  @Inject private ItemOperationFactory workflowFactory;
  private PluginTracker<ActivationImplementation> implTracker;

  @Override
  @Transactional
  public List<ActivateRequest> getAllRequestsForDateRange(
      String activationType, Item item, Date start, Date end) {
    return requestDao.getAllRequestsForDateRange(activationType, item, start, end);
  }

  @Override
  @Transactional
  public List<ActivateRequest> getAllCurrentActivations(String activationType, Item item) {
    return requestDao.getAllActiveRequests(activationType, item);
  }

  @Override
  @Transactional
  public List<ActivateRequest> getAllCurrentAndPendingActivations(
      String activationType, String attachmentUuid) {
    return requestDao.getAllActiveAndPendingRequests(activationType, attachmentUuid);
  }

  @Override
  @Transactional
  @SecureOnReturn(priv = ActivationConstants.DELETE_ACTIVATION_ITEM)
  public List<ActivateRequest> filterDeletableRequest(List<ActivateRequest> requests) {
    // Security magic
    return requests;
  }

  @Override
  @Transactional
  @SecureOnReturn(priv = ActivationConstants.EDIT_ACTIVATION_ITEM)
  public List<ActivateRequest> filterEditableRequest(List<ActivateRequest> requests) {
    List<ActivateRequest> validRequests = new ArrayList<>(requests);
    for (ActivateRequest request : requests) {
      if (request.getStatus() == ActivateRequest.TYPE_INACTIVE) {
        validRequests.remove(request);
      }
    }
    return validRequests;
  }

  @Override
  @Transactional
  @SecureOnReturn(priv = ActivationConstants.VIEW_ACTIVATION_ITEM)
  public List<ActivateRequest> getAllRequests(String activationType, Item item) {
    return getImplementation(activationType).getAllRequests(item);
  }

  @Override
  @Transactional
  public ActivateRequest getRequest(long id) {
    return requestDao.findById(id);
  }

  @Override
  @Transactional
  @SecureOnCall(priv = ActivationConstants.COPYRIGHT_ITEM)
  public List<ActivateRequest> activate(
      String activationType, Item item, List<ActivateRequest> requests, boolean skipPercentage) {
    ActivateOperation cal = operationFactory.createActivate(activationType);
    cal.setRequests(requests);
    cal.setSkipPercentage(skipPercentage);
    doOperationWithReindex(new ItemIdKey(item), cal);
    return cal.getActivatedRequests();
  }

  @Override
  @Transactional
  public void activateAll(
      String activationType, Map<Long, List<ActivateRequest>> requestMap, boolean ignorePercenage) {
    for (Map.Entry<Long, List<ActivateRequest>> entry : requestMap.entrySet()) {
      activate(activationType, itemDao.findById(entry.getKey()), entry.getValue(), ignorePercenage);
    }
  }

  @Override
  @Transactional
  public void ensureStatesForItem(Item item) {
    List<ActivateRequest> requests = requestDao.getAllRequests(item);
    ensureStates(requests);
  }

  @Override
  public void ensureStates(List<ActivateRequest> requests) {
    Date now = new Date();
    for (ActivateRequest request : requests) {
      Date from = request.getFrom();
      Date until = request.getUntil();
      int status = ActivateRequest.TYPE_PENDING;
      if (now.after(from)) {
        if (now.before(until)) {
          status = ActivateRequest.TYPE_ACTIVE;
        } else {
          status = ActivateRequest.TYPE_INACTIVE;
        }
      }
      request.setStatus(status);
    }
  }

  @Override
  @Transactional
  public List<ActivateRequest> getByCourse(CourseInfo course) {
    return requestDao.getAllRequestsForCourse(course);
  }

  @Override
  public UtcDate[] getDefaultCourseDates(CourseInfo course) {
    UtcDate from = null;
    UtcDate until = null;
    UtcDate now = new LocalDate(new UtcDate(), CurrentTimeZone.get()).conceptualDate();
    boolean useGlobalStart = course == null;
    boolean useGlobalEnd = useGlobalStart;
    // redundant course != null to stop compiler warning
    if (!useGlobalStart && course != null) {
      Date courseFrom = course.getFrom();
      if (courseFrom != null) {
        from = new UtcDate(courseFrom).conceptualDate();
      } else {
        useGlobalStart = true;
      }
      Date courseUntil = course.getUntil();
      if (courseUntil != null) {
        until = new UtcDate(courseUntil).conceptualDate();
      } else {
        useGlobalEnd = true;
      }
    }
    if (useGlobalStart) {
      CourseDefaultsSettings persistedSettings =
          systemConstantsService.getProperties(new CourseDefaultsSettings());
      from = parseCourseDate(from, persistedSettings.getStartDate());
    }
    if (useGlobalEnd) {
      CourseDefaultsSettings persistedSettings =
          systemConstantsService.getProperties(new CourseDefaultsSettings());
      until = parseCourseDate(until, persistedSettings.getEndDate());
    }
    if (from != null) {
      if (from.before(now)) {
        from = now;
      }
      UtcDate dayAfter = from.addDays(1);
      if (until != null && until.before(dayAfter)) {
        until = dayAfter;
      }
    }
    return new UtcDate[] {from, until};
  }

  private UtcDate parseCourseDate(UtcDate defaultDate, String val) {
    if (!Check.isEmpty(val)) {
      SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
      dateFormat.setTimeZone(DateHelper.UTC_TIMEZONE);
      try {
        return new UtcDate(dateFormat.parse(val)).conceptualDate();
      } catch (ParseException e) {
        // return default
      }
    }
    return defaultDate;
  }

  private void doOperationWithReindex(ItemIdKey itemIdKey, WorkflowOperation op) {
    itemService.operation(itemIdKey, op, workflowFactory.reindexOnly(false));
  }

  @Override
  @Transactional
  public boolean isActive(String activationType, Item item, String attachmentUuuid) {
    return requestDao.getLastActive(activationType, item, attachmentUuuid) != null;
  }

  @Override
  @Transactional
  public boolean isActiveOrPending(String activationType, String attachmentUuuid) {
    return !requestDao.getAllActiveAndPendingRequests(activationType, attachmentUuuid).isEmpty();
  }

  @Override
  @Transactional
  public void delete(String activationType, long id) {
    delete(activationType, requestDao.findById(id));
  }

  @Override
  public String getStatusKey(int status) {
    switch (status) {
      case ActivateRequest.TYPE_ACTIVE:
        return r.key("status.active"); // $NON-NLS-1$
      case ActivateRequest.TYPE_PENDING:
        return r.key("status.pending"); // $NON-NLS-1$
      case ActivateRequest.TYPE_INACTIVE:
        return r.key("status.inactive"); // $NON-NLS-1$
    }
    return null;
  }

  @Override
  @Transactional
  public Collection<ItemId> getAllActivatedItemsForInstitution() {
    return requestDao.getAllActivatedItemsForInstitution();
  }

  @Override
  @Transactional
  public ActivateRequest getRequest(String requestUuid) {
    return requestDao.getByUuid(requestUuid);
  }

  @Override
  @Transactional
  @SecureOnCall(priv = ActivationConstants.DEACTIVATE_ACTIVATION_ITEM)
  public void deactivate(String activationType, ActivateRequest request) {
    doOperationWithReindex(
        new ItemIdKey(request.getItem()), operationFactory.createDeactivate(request.getId()));
  }

  @Override
  @Transactional
  @SecureOnCall(priv = ActivationConstants.DELETE_ACTIVATION_ITEM)
  public void delete(String activationType, ActivateRequest request) {
    doOperationWithReindex(
        new ItemIdKey(request.getItem()), operationFactory.createDelete(request.getId()));
  }

  @Override
  @Transactional
  @SecureOnReturn(priv = ActivationConstants.COPYRIGHT_ITEM)
  public Set<Item> filterActivatableItems(Set<Item> portionItems) {
    return portionItems;
  }

  private ActivationImplementation getImplementation(String type) {
    final ActivationImplementation impl = implTracker.getBeanMap().get(type);
    if (impl == null) {
      throw new RuntimeException("No activation implementation for type " + type);
    }
    return impl;
  }

  @Override
  public List<String> getImplementationTypes() {
    final List<String> implTypes = new ArrayList<String>();
    for (Map.Entry<String, ?> impl : implTracker.getBeanMap().entrySet()) {
      implTypes.add(impl.getKey());
    }
    return implTypes;
  }

  @Inject
  public void setPluginService(PluginService pluginService) {
    implTracker =
        new PluginTracker<ActivationImplementation>(
            pluginService, "com.tle.core.activation", "activationImplementation", "type");
    implTracker.setBeanKey("class");
  }

  @Override
  @Transactional
  public List<ActivateRequest> getAllByStatus(int status) {
    return requestDao.getAllRequestsByStatus(status);
  }

  @Override
  @Transactional
  public List<ActivateRequest> listAll() {
    return requestDao.listAll();
  }

  @Override
  @Transactional
  public List<ActivateRequest> getAllRequests(Item item) {
    return requestDao.getAllRequests(item);
  }

  @Override
  public void validateItem(
      String activationType, Item item, boolean ignoreOverrides, boolean skipPercentage) {
    getImplementation(activationType).validateItem(item, ignoreOverrides, skipPercentage);
  }

  @Override
  public String getActivationDescription(ActivateRequest request) {
    return getImplementation(request.getType()).getActivationDescription(request);
  }

  @Override
  @Transactional
  public void deactivateByUuid(String activationUuid) {
    ActivateRequest request = requestDao.getByUuid(activationUuid);
    if (request == null) {
      throw new NotFoundException("No activationRequest with uuid " + activationUuid);
    }
    deactivate(request.getType(), request);
  }

  @Override
  public void setup(ItemKey itemId, ItemOperationParams params, Item item) {
    params.setAttribute(ItemActivationId.PARAM_KEY, ((ItemActivationId) itemId).getActivationId());
  }

  @Override
  public void userDeletedEvent(UserDeletedEvent event) {
    // Activations should hang around, even if a user is deleted, since the
    // purpose of activations to make a resource available to others.
  }

  @Override
  public void userEditedEvent(UserEditEvent event) {
    // Don't care
  }

  @Override
  @Transactional
  public void userIdChangedEvent(UserIdChangedEvent event) {
    // Should probably be doing this as a filter instead....
    ReassignActivationOperation op =
        operationFactory.reassignActivations(event.getFromUserId(), event.getToUserId());
    for (ItemIdKey key : requestDao.getItemKeysForUserActivations(event.getFromUserId())) {
      doOperationWithReindex(key, op);
    }
  }

  @Override
  @Transactional
  @SecureOnCall(priv = ActivationConstants.EDIT_ACTIVATION_ITEM)
  public void updateActivation(ActivateRequest request) {
    requestDao.update(request);
  }

  @Override
  public boolean attachmentIsSelectableForCourse(
      String activationType, String attachmentUuid, String courseCode) {
    final CourseDefaultsSettings courseSettings =
        systemConstantsService.getProperties(new CourseDefaultsSettings());
    if (courseSettings.isPortionRestrictionsEnabled()) {
      final List<ActivateRequest> requests =
          requestDao.getAllActiveAndPendingRequests(activationType, attachmentUuid);
      return requests.stream()
          .filter(r -> r.getCourse().getCode().equals(courseCode))
          .findFirst()
          .isPresent();
    }
    return true;
  }
}
