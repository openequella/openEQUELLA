package com.tle.web.remoting.graphql

import caliban.ResponseValue.ObjectValue
import caliban.Value.StringValue
import com.dytech.edge.common.LockedException
import com.dytech.edge.exceptions.InUseException
import com.tle.beans.item.ItemEditingException
import com.tle.common.beans.exception.{InvalidDataException, NotFoundException}
import com.tle.exceptions.AccessDeniedException
import com.tle.web.remoting.graphql.ErrorCodes._
import org.apache.catalina.connector.ClientAbortException

import java.io.IOException

/**
  * Codifies all the possible error codes we may return to a client.
  */
object ErrorCodes {
  val ACCESS_DENIED  = "access_denied"
  val BAD_REQUEST    = "bad_request"
  val CLIENT_ABORT   = "client_abort"
  val INTERNAL_ERROR = "internal_error"
  val IO_ERROR       = "io_error"
  val LOCKED         = "locked"
  val NOT_FOUND      = "not_found"
}

/**
  * Utility object for handling during the processing of GraphQL requests.
  */
object Errors {

  /**
    * Maps an exception to a string error code. Based on
    * com.tle.web.remoting.resteasy.RestEasyExceptionMapper#mapException(java.lang.Throwable).
    *
    * @param throwable the exception to map
    * @return the error code
    */
  def mapException(throwable: Throwable): String = throwable match {
    case _: ItemEditingException | _: InvalidDataException | _: InUseException => BAD_REQUEST
    case _: AccessDeniedException                                              => ACCESS_DENIED
    case _: LockedException                                                    => LOCKED
    case _: NotFoundException | _: javax.ws.rs.NotFoundException               => NOT_FOUND
    case _: ClientAbortException                                               => CLIENT_ABORT
    case _: IOException                                                        => IO_ERROR
    case _                                                                     => INTERNAL_ERROR
  }

  /**
    * Standardises the building of the `cause` `extension` object for an error. We use `cause` to
    * report a string error code (from `ErrorCodes`) to the client.
    *
    * @param cause the value to put in `cause` - ideally an error code from `ErrorCodes`
    * @return an `ObjectValue` to be added to `extensions`
    */
  def buildCauseObjectValue(cause: String): ObjectValue =
    ObjectValue(List("cause" -> StringValue(cause)))

  /**
    * Standardises the building of the `cause` `extension` object for an error for exceptions.
    *
    * @see #buildCauseObjectValue(String)
    * @param cause an Exception which caused an error that is to be converted to a standard error code
    * @return an `ObjectValue` to be added to `extensions`
    */
  def buildCauseObjectValue(cause: Throwable): ObjectValue =
    buildCauseObjectValue(mapException(cause))
}
