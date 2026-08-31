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

package com.tle.core.migration;

/** A single step of the upgrade applied to an institution's schema or data. */
public interface Migration {
  /**
   * Whether an older build - one which does not know this migration ran - can still work against
   * the institution afterwards.
   *
   * <p>Rewriting the values held in a column is backwards compatible, because the shape the older
   * build expects has not changed. Dropping a column, renaming one, or changing its type is not.
   *
   * <p>What this decides in practice is whether a later release may delete the migration. One which
   * answers true can be dropped from the codebase once it is old enough, and an institution which
   * ran it still upgrades without it. One which answers false cannot: its log entry is marked as
   * having to exist, and a build which no longer carries it refuses the upgrade outright with
   * "missing required backwards incompatible migration".
   */
  boolean isBackwardsCompatible();

  /**
   * Does the work of the migration. Implementations report progress through the given {@link
   * MigrationResult} so that the upgrade screen can show how far along they are.
   */
  void migrate(MigrationResult status) throws Exception;

  /**
   * Describes this migration to the administrator running the upgrade. The name it is created with
   * is a language string key, resolved against the plugin's i18n properties.
   */
  MigrationInfo createMigrationInfo();
}
