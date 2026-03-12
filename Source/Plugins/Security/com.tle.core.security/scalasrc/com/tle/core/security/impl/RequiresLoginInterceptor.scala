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

package com.tle.core.security.impl

import com.google.inject.Singleton
import com.tle.common.usermanagement.user.CurrentUser
import com.tle.core.guice.Bind
import com.tle.exceptions.AuthenticationException
import org.aopalliance.intercept.{MethodInterceptor, MethodInvocation}

/** An interceptor that checks for the presence of the `@RequiresLogin` annotation on a method or
  * class, and if found, ensures that the current user is not a guest. If the user is not login in,
  * an `AuthenticationException` is thrown with the message specified in the annotation.
  */
@Bind
@Singleton
class RequiresLoginInterceptor extends MethodInterceptor {

  override def invoke(invocation: MethodInvocation): AnyRef = getAnnotation(invocation) match {
    case Some(ann) if CurrentUser.isGuest =>
      throw new AuthenticationException(ann.message)
    case _ =>
      invocation.proceed()
  }

  private def getAnnotationFromMethod(invocation: MethodInvocation): Option[RequiresLogin] =
    Option(invocation.getMethod.getAnnotation(classOf[RequiresLogin]))

  private def getAnnotationFromClass(invocation: MethodInvocation): Option[RequiresLogin] =
    Option(invocation.getMethod.getDeclaringClass.getAnnotation(classOf[RequiresLogin]))

  private def getAnnotation(invocation: MethodInvocation): Option[RequiresLogin] =
    getAnnotationFromMethod(invocation).orElse(getAnnotationFromClass(invocation))
}
