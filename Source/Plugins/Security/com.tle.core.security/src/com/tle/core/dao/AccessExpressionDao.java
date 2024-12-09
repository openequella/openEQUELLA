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

package com.tle.core.dao;

import com.tle.beans.security.AccessExpression;
import com.tle.common.Triple;
import com.tle.core.hibernate.dao.GenericDao;
import java.util.List;
import java.util.Map;

public interface AccessExpressionDao extends GenericDao<AccessExpression, Long> {
  AccessExpression retrieveOrCreate(String expression);

  void deleteOrphanedExpressions();

  List<Triple<Long, String, Boolean>> getMatchingExpressions(List<String> values);

  List<AccessExpression> listAll();

  Map<Long, Long> userIdChanged(String fromUserId, String toUserId);

  Map<Long, Long> groupIdChanged(String fromGroupId, String toGroupId);
}
