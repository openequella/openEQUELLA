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

package com.tle.integration.oauth2.error

/**
  * Represent any error that can occur during the integration of OAuth2, including standard errors
  * defined in the spec and other general errors.
  */
trait OAuth2Error {

  /**
    * Optional message to provide more details of the error.
    */
  val msg: Option[String]
}

/**
  * Represent an error that has a unique error code
  */
trait HasCode[C] {
  val code: C
}
