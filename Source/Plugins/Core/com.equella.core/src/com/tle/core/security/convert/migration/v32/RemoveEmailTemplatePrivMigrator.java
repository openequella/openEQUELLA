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

package com.tle.core.security.convert.migration.v32;

import com.tle.beans.security.AccessEntry;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.PostReadMigrator;
import com.tle.core.security.convert.AclConverter.AclPostReadMigratorParams;
import java.io.IOException;
import java.util.Iterator;
import javax.inject.Singleton;

@Bind
@Singleton
public class RemoveEmailTemplatePrivMigrator
    implements PostReadMigrator<AclPostReadMigratorParams> {
  @Override
  public void migrate(AclPostReadMigratorParams list) throws IOException {
    Iterator<AccessEntry> entries = list.iterator();
    while (entries.hasNext()) {
      AccessEntry entry = entries.next();
      if (entry.getPrivilege().endsWith("EMAIL_TEMPLATE")) {
        entries.remove();
      }
    }
  }
}
