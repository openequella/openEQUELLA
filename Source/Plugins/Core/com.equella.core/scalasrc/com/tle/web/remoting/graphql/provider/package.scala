package com.tle.web.remoting.graphql

import com.tle.common.beans.exception.NotFoundException

package object provider {

  /** A utility method to wrap a function that may throw a `NotFoundException` and return an
    * `Option`. If the function throws a `NotFoundException`, it returns `None`, otherwise it
    * returns `Some(value)`.
    *
    * @param fn
    *   the function to execute
    * @tparam T
    *   the type of the value returned by the function
    * @return
    *   an `Option[T]` that is `None` if the function throws a `NotFoundException`, otherwise `Some`
    *   with the result of the function
    */
  def noneIfNotFound[T](fn: => T): Option[T] = {
    try {
      Option(fn)
    } catch {
      case _: NotFoundException => None
    }
  }
}
