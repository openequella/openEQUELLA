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
 * Enforces security checks before method execution.
 *
 * <p>This annotation prevents unauthorised method execution by checking user permissions against a
 * domain object parameter. The method will only execute if the current user has the specified
 * privilege on the identified domain object. This is particularly useful for:
 *
 * <ul>
 *   <li>Protecting operations that modify data
 *   <li>Enforcing access control at the method level
 *   <li>Preventing unauthorised access to sensitive functionality
 * </ul>
 *
 * @see SecureOnReturn
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SecureOnCall {
  /**
   * The privilege required to execute this method. The method will only be called if the current
   * user has this privilege on the identified domain object parameter.
   *
   * @return the privilege string identifier
   */
  String priv() default "";
}
