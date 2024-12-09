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

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.tle.common.util.ExecUtils;
import com.tle.upgrade.LineFileModifier;
import com.tle.upgrade.UpgradeDepends;
import com.tle.upgrade.UpgradeResult;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AddLDAPPoolingOptions extends AbstractUpgrader {
  @Override
  public List<UpgradeDepends> getDepends() {
    List<UpgradeDepends> depends = new ArrayList<>();
    depends.add(new UpgradeDepends(UpgradeToEmbeddedTomcat.ID));
    return depends;
  }

  @Override
  public String getId() {
    return "AddLDAPPoolingOptions";
  }

  @Override
  public boolean isBackwardsCompatible() {
    return true;
  }

  @Override
  public void upgrade(UpgradeResult result, File tleInstallDir) throws Exception {
    final boolean windows = ExecUtils.determinePlatform().startsWith(ExecUtils.PLATFORM_WIN);
    final String delim = windows ? ";" : " ";
    final String config =
        windows ? "manager/equellaserver-config.bat" : "manager/equellaserver-config.sh";

    final File file = new File(tleInstallDir, config);
    if (file.exists()) {
      new LineFileModifier(file, result) {
        @Override
        protected String processLine(String line) {
          if (line.startsWith("set JAVA_ARGS=")) {
            return addPoolingOpts(line, "set JAVA_ARGS=", delim);
          } else if (line.startsWith("export JAVA_OPTS=")) {
            return addPoolingOpts(line, "export JAVA_OPTS=\"", delim);
          }
          return line;
        }
      }.update();
    }
  }

  private String addPoolingOpts(String line, String prefix, String delim) {
    List<String> opts =
        Lists.newArrayList(Splitter.on(delim).split(line.substring(prefix.length())));
    addIfAbsent(opts, "-Dcom.sun.jndi.ldap.connect.pool.timeout=", "3000000");
    addIfAbsent(opts, "-Dcom.sun.jndi.ldap.connect.pool.maxsize=", "200");
    addIfAbsent(opts, "-Dcom.sun.jndi.ldap.connect.pool.prefsize=", "20");
    return prefix + Joiner.on(delim).join(opts);
  }

  private boolean addIfAbsent(List<String> opts, String key, String value) {
    for (int i = 0; i < opts.size(); i++) {
      String opt = opts.get(i);
      if (opt.startsWith(key)) {
        return false;
      }
    }
    opts.add(2, key + value);
    return true;
  }
}
