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

package com.tle.core.auditlog.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.beans.audit.AuditLogEntry;
import com.tle.core.auditlog.AuditLogDao;
import com.tle.core.guice.Bind;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

@Bind(AuditLogDao.class)
@Singleton
public class AuditLogDaoImpl extends AbstractAuditLogDaoImpl<AuditLogEntry> implements AuditLogDao {
  public AuditLogDaoImpl() {
    super(AuditLogEntry.class);
  }

  @Override
  public void logWithRequest(AuditLogEntry entry, HttpServletRequest request) {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode node = mapper.createObjectNode();
    node.put("referrer", request.getHeader("Referer"));
    entry.setMeta(node.toString());

    save(entry);
  }
}
