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

package com.tle.core.item.standard.filter.workflow;

import com.tle.beans.item.ItemStatus;
import com.tle.core.guice.Bind;
import com.tle.core.item.operations.AbstractWorkflowOperation;
import com.tle.core.item.standard.filter.AbstractStandardOperationFilter;
import java.util.Map;

@SuppressWarnings("nls")
@Bind
public class ReviewFilter extends AbstractStandardOperationFilter {
  @Override
  public AbstractWorkflowOperation[] createOperations() {
    return new AbstractWorkflowOperation[] {
      operationFactory.review(false), operationFactory.save()
    };
  }

  @Override
  public void queryValues(Map<String, Object> values) {
    values.put("reviewDate", getDateNow());
    values.put("status", ItemStatus.LIVE.name());
  }

  @Override
  public String getJoinClause() {
    return "join i.moderation as m";
  }

  @Override
  public String getWhereClause() {
    return "moderating = false and status = :status and m.reviewDate <= :reviewDate";
  }
}
