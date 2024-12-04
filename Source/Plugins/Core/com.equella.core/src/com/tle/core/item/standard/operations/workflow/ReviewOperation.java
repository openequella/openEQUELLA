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

package com.tle.core.item.standard.operations.workflow;

import com.dytech.edge.exceptions.WorkflowException;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.ModerationStatus;
import com.tle.core.i18n.CoreStrings;
import com.tle.core.security.impl.SecureItemStatus;
import com.tle.core.security.impl.SecureOnCall;
import com.tle.exceptions.AccessDeniedException;
import java.util.Date;

@SecureItemStatus({ItemStatus.LIVE, ItemStatus.ARCHIVED})
@SecureOnCall(priv = "REVIEW_ITEM")
public class ReviewOperation extends TaskOperation {
  private final boolean force;

  @AssistedInject
  protected ReviewOperation() {
    this(true);
  }

  @AssistedInject
  protected ReviewOperation(@Assisted boolean force) {
    this.force = force;
  }

  /**
   * @throws WorkflowException
   */
  @SuppressWarnings("nls")
  @Override
  public boolean execute() {
    if (getWorkflow() == null) {
      throw new AccessDeniedException(CoreStrings.text("error.noworkflow"));
    }

    ModerationStatus status = getModerationStatus();
    Date reviewdate = status.getReviewDate();
    Date datenow = getParams().getDateNow();
    boolean needsReview = reviewdate != null && (datenow.compareTo(reviewdate) > 0);

    if (force || needsReview) {
      // Item requires review
      setState(ItemStatus.REVIEW);
      resetWorkflow();
      updateModeration();
      return true;
    }
    return false;
  }
}
