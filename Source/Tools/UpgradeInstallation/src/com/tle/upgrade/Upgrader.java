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

package com.tle.upgrade;

import java.io.File;
import java.util.List;

public interface Upgrader {
  String getId();

  boolean isRunOnInstall();

  /**
   * Whether this upgrader can be removed from the code base in future releases. If true, the
   * upgrader will be marked as not having to exist (mustExist = false) in the upgrade log.
   *
   * <p>If this is set to true, ideally the author should add a note in the javadoc as to when it
   * can be removed.
   */
  boolean canBeRemoved();

  List<UpgradeDepends> getDepends();

  void upgrade(UpgradeResult result, File tleInstallDir) throws Exception;
}
