package io.github.openequella.graphql

package object api {

  /**
    * Flattens the result of an operation that returns an `Either[List[ApiError], Option[A]]` to an
    * `Either[List[ApiError], A]`. This is needed because Caliban returns an `Option[A]` for
    * operations that return an effect representing any operation which can fail.
    *
    * See more at: <https://ghostdogpr.github.io/caliban/faq/#the-auto-generated-schema-shows-a-field-is-nullable-but-i-want-it-non-nullable-instead>
    */
  def flattenResult[A](result: Either[List[ApiError], Option[A]]): Either[List[ApiError], A] =
    result.flatMap {
      case Some(a) => Right(a)
      case None    => Left(List(UnknownError("Although operation successful, no data was returned.")))
    }
}
