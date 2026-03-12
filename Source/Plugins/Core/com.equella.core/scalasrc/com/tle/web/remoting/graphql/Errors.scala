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

package com.tle.web.remoting.graphql

import caliban.ResponseValue.ObjectValue
import caliban.Value.StringValue
import com.dytech.edge.common.LockedException
import com.dytech.edge.exceptions.InUseException
import com.tle.beans.item.ItemEditingException
import com.tle.common.beans.exception.{InvalidDataException, NotFoundException}
import com.tle.exceptions.{AccessDeniedException, AuthenticationException}
import com.tle.web.remoting.graphql.ErrorCode._
import org.apache.catalina.connector.ClientAbortException

import java.io.IOException

/** Utility object for handling during the processing of GraphQL requests.
  */
object Errors {

  /** Maps an exception to a string error code. Based on
    * com.tle.web.remoting.resteasy.RestEasyExceptionMapper#mapException(java.lang.Throwable).
    *
    * @param throwable
    *   the exception to map
    * @return
    *   the error code
    */
  def mapException(throwable: Throwable): ErrorCode.Code = throwable match {
    case _: ItemEditingException | _: InvalidDataException       => BAD_REQUEST
    case _: AccessDeniedException | _: AuthenticationException   => ACCESS_DENIED
    case _: LockedException                                      => LOCKED
    case _: NotFoundException | _: javax.ws.rs.NotFoundException => NOT_FOUND
    case _: ClientAbortException                                 => CLIENT_ABORT
    case _: IOException                                          => IO_ERROR
    case _: InUseException                                       => IN_USE
    case _                                                       => INTERNAL_ERROR
  }

  /** Standardises the building of the `cause` `extension` object for an error. We use `cause` to
    * report a string error code (from `ErrorCodes`) to the client.
    *
    * @param cause
    *   the value to put in `cause` - ideally an error code from `ErrorCodes`
    * @return
    *   an `ObjectValue` to be added to `extensions`
    */
  def buildCauseObjectValue(cause: String): ObjectValue =
    ObjectValue(List("cause" -> StringValue(cause)))

  /** Standardises the building of the `cause` `extension` object for an error for exceptions.
    *
    * @see
    *   #buildCauseObjectValue(String)
    * @param cause
    *   an Exception which caused an error that is to be converted to a standard error code
    * @return
    *   an `ObjectValue` to be added to `extensions`
    */
  def buildCauseObjectValue(cause: Throwable): ObjectValue =
    buildCauseObjectValue(mapException(cause).toString)
}
