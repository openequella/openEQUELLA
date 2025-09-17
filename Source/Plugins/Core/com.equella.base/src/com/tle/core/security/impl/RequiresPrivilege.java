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

package com.tle.core.security.impl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Ensures the current user possesses a specific privilege before allowing method execution.
 *
 * <p>This annotation performs a security check that is not tied to a specific domain object. It
 * verifies that the user has the required privilege granted at <strong>any</strong> level within
 * the system, whether on a specific entity or at the institutional level. This is ideal for
 * securing functionality that is not context-dependent.
 *
 * <p>Use this for:
 *
 * <ul>
 *   <li>Securing access to system-wide administrative functions.
 *   <li>Restricting use of tools or APIs that are not tied to a particular item.
 *   <li>Verifying a user's general capability before allowing an action.
 * </ul>
 *
 * @see SecureOnCall
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPrivilege {
  /**
   * The privilege required to execute the annotated method. The method will only be invoked if the
   * current user has been granted this privilege at any scope within the application.
   *
   * @return the privilege string identifier
   */
  String priv() default "";
}
