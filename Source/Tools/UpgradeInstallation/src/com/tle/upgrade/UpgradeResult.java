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

package com.tle.upgrade;

import org.apache.commons.logging.Log;

public class UpgradeResult {
  private boolean canRetry;
  private boolean retry;
  private String message;
  private StringBuilder workLog = new StringBuilder();
  private Log log;

  public UpgradeResult(Log logger) {
    this.log = logger;
  }

  public boolean isCanRetry() {
    return canRetry;
  }

  public void setCanRetry(boolean canRetry) {
    this.canRetry = canRetry;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public void addLogMessage(String message) {
    workLog.append(message);
    workLog.append('\n');
  }

  public void info(String message) {
    log.info(message);
  }

  public String getWorkLog() {
    return workLog.toString();
  }

  public boolean isRetry() {
    return retry;
  }

  public void setRetry(boolean retry) {
    this.retry = retry;
  }
}
