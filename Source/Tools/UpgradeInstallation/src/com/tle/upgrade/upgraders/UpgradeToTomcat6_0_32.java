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

package com.tle.upgrade.upgraders;

import com.tle.upgrade.UpgradeDepends;
import com.tle.upgrade.UpgradeResult;
import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Obsolete
 *
 * @author aholland
 */
@Deprecated
@SuppressWarnings("nls")
public class UpgradeToTomcat6_0_32 extends AbstractUpgrader {
  public static final String ID = "UpgradeTomcat6_0_32";

  @Override
  public void upgrade(UpgradeResult result, File tleInstallDir) throws Exception {
    obsoleteError();
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public List<UpgradeDepends> getDepends() {
    UpgradeDepends dep = new UpgradeDepends(UpgradeToTomcat6_0_26.ID);
    dep.setObsoletes(true);
    return Collections.singletonList(dep);
  }

  @Override
  public boolean canBeRemoved() {
    return false;
  }
}
