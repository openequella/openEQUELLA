/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import com.tle.core.migration.log.MigrationLog;

public class MigrationState {
  private final MigrationExt extension;
  private final MigrationLog logEntry;
  private boolean skip;
  private boolean execute;
  private boolean obsoleted;

  public MigrationState(MigrationExt extension, MigrationLog logEntry) {
    this.extension = extension;
    this.logEntry = logEntry;
  }

  public boolean isPlaceHolder() {
    return extension.placeholder();
  }

  public boolean needsProcessing() {
    return logEntry == null || logEntry.getStatus() == MigrationLog.LogStatus.ERRORED;
  }

  public boolean wasSkippedAlready() {
    return logEntry != null && logEntry.getStatus() == MigrationLog.LogStatus.SKIPPED;
  }

  public boolean wasExecutedAlready() {
    return logEntry != null && logEntry.getStatus() == MigrationLog.LogStatus.EXECUTED;
  }

  public String getId() {
    return extension.id();
  }

  public boolean isCanRetry() {
    return logEntry != null && logEntry.isCanRetry();
  }

  public MigrationLog.LogStatus getStatus() {
    if (logEntry == null) {
      return null;
    }
    return logEntry.getStatus();
  }

  public MigrationLog getLogEntry() {
    return logEntry;
  }

  public boolean isSkip() {
    return skip;
  }

  public void setSkip(boolean skip) {
    this.skip = skip;
  }

  public boolean isObsoleted() {
    return obsoleted;
  }

  public void setObsoleted(boolean obsoleted) {
    this.obsoleted = obsoleted;
  }

  public boolean isExecute() {
    return execute;
  }

  public void setExecute(boolean execute) {
    this.execute = execute;
  }

  public Migration getMigration() {
    return extension.migration();
  }
}
