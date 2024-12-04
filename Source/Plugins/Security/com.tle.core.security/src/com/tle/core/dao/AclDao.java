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

import com.tle.beans.Institution;
import com.tle.beans.security.ACLEntryMapping;
import com.tle.beans.security.AccessEntry;
import com.tle.common.security.TargetListEntry;
import com.tle.core.hibernate.dao.GenericDao;
import java.util.Collection;
import java.util.List;

public interface AclDao extends GenericDao<AccessEntry, Long> {
  List<Object[]> getPrivileges(Collection<String> privileges, Collection<Long> expressions);

  List<Object[]> getPrivilegesForTargets(
      Collection<String> privileges, Collection<String> targets, Collection<Long> expressions);

  void delete(String target, String privilege, Institution institution);

  void deleteAll(String target, boolean targetIsPartial, List<Integer> priorities);

  List<TargetListEntry> getTargetListEntries(String target, Collection<Integer> priorities);

  List<ACLEntryMapping> getAllEntries(Collection<String> privilege, Collection<String> targets);

  List<AccessEntry> listAll();

  void deleteAll();

  void remapExpressionId(long oldId, long newId);

  List<AccessEntry> getVirtualAccessEntries(Collection<Integer> priorities);
}
