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

package com.dytech.edge.exceptions;

/**
 * Interface for exceptions that can determine whether the should be logged and to what extent.
 *
 * @author aholland
 */
public interface QuietlyLoggable {
  /**
   * You should rarely return true, if ever
   *
   * @return true if this exception should _not_ be reported in the logs
   */
  boolean isSilent();

  /**
   * @return true if the full stack should be printed to the logs, otherwise false for a single line
   *     message
   */
  boolean isShowStackTrace();

  /**
   * @return true if it should be logged as a WARN instead of an ERROR
   * @return
   */
  boolean isWarnOnly();
}
