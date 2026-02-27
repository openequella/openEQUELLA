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

package com.tle.admin.tools.common;

import com.dytech.edge.common.LockedException;
import com.dytech.edge.exceptions.InUseException;
import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.admin.Driver;
import com.tle.admin.service.ClientRequestException;
import com.tle.common.NameValue;
import io.github.openequella.graphql.api.InUseError;
import java.awt.Component;
import java.util.List;

/**
 * Handles the removal of one or more entities selected in a {@link BaseEntityTool} list.
 *
 * <p>This worker iterates through the selected entities, deleting each via the entity service. On
 * success, it removes the entries from the UI list. On failure, it displays an appropriate message
 * depending on the error type (in-use, locked, access denied, or generic) and refreshes the list.
 *
 * @see BaseEntityTool#onRemove()
 */
class BaseEntityToolOnRemoveHandler extends GlassSwingWorker<Object> {

  private final BaseEntityTool<?> entityTool;
  private final List<NameValue> selectedEntities;
  // The entity currently being deleted; set during construct() and read during exception()
  private NameValue currentEntity;

  /**
   * Create a handler for removing the given entities.
   *
   * @param parentFrame the parent component used for dialog display and the glass pane
   * @param selectedEntities the entities selected for removal
   * @param entityTool the tool that owns the entity list and service
   */
  BaseEntityToolOnRemoveHandler(
      Component parentFrame, List<NameValue> selectedEntities, BaseEntityTool<?> entityTool) {
    this.selectedEntities = selectedEntities;
    this.entityTool = entityTool;
    setComponent(parentFrame);
  }

  @Override
  public Object construct() {
    for (NameValue entity : selectedEntities) {
      currentEntity = entity;
      entityTool.remove(Long.parseLong(entity.getValue()));
    }
    return null;
  }

  @Override
  public void finished() {
    entityTool.removeSelectedObjects();
    Driver.displayInformation(
        getComponent(), BaseEntityTool.s("deleted", entityTool.getEntityNameNormal()));
  }

  @Override
  public void exception() {
    Exception exception = getException();

    switch (exception) {
      case Exception e when isInUseException(e) -> displayInUseMessage(currentEntity, e);
      case LockedException ignored ->
          Driver.displayInformation(
              getComponent(), BaseEntityTool.s("cannotdelete.locked", currentEntity));
      case Exception e when "Access is denied".equals(e.getMessage()) ->
          Driver.displayInformation(
              getComponent(), BaseEntityTool.s("nopermission", currentEntity));
      default -> {
        Driver.displayError(getComponent(), entityTool.getDeletingErrorMessage(), exception);
        BaseEntityTool.LOGGER.error(
            "Could not delete " + entityTool.getEntityNameLower() + " " + currentEntity.getValue(),
            exception);
      }
    }
    entityTool.refreshAndSelect();
  }

  private boolean isInUseException(Exception exception) {
    return switch (exception) {
      case InUseException ignored -> true;
      case ClientRequestException e -> e.getApiErrorOfType(InUseError.class).isDefined();
      default -> false;
    };
  }

  private void displayInUseMessage(NameValue failedEntity, Exception exception) {
    final String defaultAdditionalDetail = formatInUseBy("Unknown");
    // Slightly unconventional, but the type of entity which is causing the in-use error is simply
    // passed in the message of the InUseException. Although this is not the case for the
    // ClientRequestException which is a standard error message with details.
    final String additionalDetail =
        switch (exception) {
          case InUseException e -> formatInUseBy(e.getMessage());
          case ClientRequestException e ->
              e.getApiErrorOfType(InUseError.class)
                  .map(InUseError::message)
                  .getOrElse(() -> defaultAdditionalDetail);
          default -> defaultAdditionalDetail;
        };
    Driver.displayInformation(
        getComponent(), BaseEntityTool.s("cannotdelete.inuse", failedEntity, additionalDetail));
  }

  private String formatInUseBy(String entityType) {
    return BaseEntityTool.s("cannotdelete.inuseby", entityType);
  }
}
