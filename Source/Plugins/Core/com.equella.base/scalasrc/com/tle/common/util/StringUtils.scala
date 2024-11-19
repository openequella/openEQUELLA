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

package com.tle.common.util

import java.security.SecureRandom

object StringUtils {

  private val secureRandom = SecureRandom.getInstanceStrong

  /**
    * Generates a string of random bytes represented as hexadecimal values.
    *
    * @param length number of random bytes
    * @return a string which is twice the length of `length` with each two characters representing
    *         one byte
    */
  def generateRandomHexString(length: Int): String =
    Range(0, length).map(_ => "%02x".format(secureRandom.nextInt(255))).mkString
}
