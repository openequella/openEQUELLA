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

package com.tle.core.workflow.standard.filter;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.core.item.operations.BaseFilter;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.standard.ItemOperationFactory;
import java.util.Collection;
import java.util.Map;
import javax.inject.Inject;

public class CheckModerationForStepsFilter extends BaseFilter {
  private final Collection<Long> changedNodes;
  private final boolean forceSave;

  @Inject private ItemOperationFactory itemOperationFactory;

  @AssistedInject
  protected CheckModerationForStepsFilter(
      @Assisted Collection<Long> changedNodes, @Assisted boolean forceSave) {
    this.changedNodes = changedNodes;
    this.forceSave = forceSave;
  }

  @Override
  public WorkflowOperation[] createOperations() {
    if (forceSave) {
      return new WorkflowOperation[] {
        itemOperationFactory.checkSteps(),
        itemOperationFactory.forceModify(),
        itemOperationFactory.saveUnlock(false)
      };
    }
    return new WorkflowOperation[] {
      itemOperationFactory.checkSteps(), itemOperationFactory.saveUnlock(false)
    };
  }

  @Override
  public void queryValues(Map<String, Object> values) {
    values.put("tasks", changedNodes); // $NON-NLS-1$
  }

  @SuppressWarnings("nls")
  @Override
  public String getWhereClause() {
    return "s.node.id in (:tasks)";
  }

  @SuppressWarnings("nls")
  @Override
  public String getJoinClause() {
    return "join i.moderation.statuses s";
  }
}
