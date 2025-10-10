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

import java.util.Date;

public class UpgradeLog {
  public enum LogStatus {
    EXECUTED,
    SKIPPED,
    OBSOLETE,
    ERRORED
  }

  private String migrationId;

  private Date executed;

  private boolean mustExist;
  private boolean canRetry;

  private LogStatus status;
  private String message;
  private String log;
  private String errorMessage;

  public Date getExecuted() {
    return executed;
  }

  public void setExecuted(Date executed) {
    this.executed = executed;
  }

  public String getMigrationId() {
    return migrationId;
  }

  public void setMigrationId(String migrationId) {
    this.migrationId = migrationId;
  }

  public boolean isMustExist() {
    return mustExist;
  }

  /**
   * Indicates that this upgrader must exist in future upgrades. If it does not, an error will be
   * raised.
   */
  public void setMustExist(boolean mustExist) {
    this.mustExist = mustExist;
  }

  public LogStatus getStatus() {
    return status;
  }

  public void setStatus(LogStatus status) {
    this.status = status;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public boolean isCanRetry() {
    return canRetry;
  }

  public void setCanRetry(boolean canRetry) {
    this.canRetry = canRetry;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public String getLog() {
    return log;
  }

  public void setLog(String log) {
    this.log = log;
  }
}
