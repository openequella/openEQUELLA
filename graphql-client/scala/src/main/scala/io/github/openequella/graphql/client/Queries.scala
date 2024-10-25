package io.github.openequella.graphql.client

import caliban.client.FieldBuilder._
import caliban.client._

object Queries {

  /** List all internal users, optionally filtered by a query
    */
  def internalUsers[A](
      query: scala.Option[String] = None,
      first: scala.Option[Int] = None,
      last: scala.Option[Int] = None,
      before: scala.Option[String] = None,
      after: scala.Option[String] = None
  )(innerSelection: SelectionBuilder[UserConnection, A])(implicit
                                                         encoder0: ArgEncoder[scala.Option[String]],
                                                         encoder1: ArgEncoder[scala.Option[Int]],
                                                         encoder2: ArgEncoder[scala.Option[Int]],
                                                         encoder3: ArgEncoder[scala.Option[String]],
                                                         encoder4: ArgEncoder[scala.Option[String]])
    : SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "internalUsers",
      OptionOf(Obj(innerSelection)),
      arguments = List(
        Argument("query", query, "String")(encoder0),
        Argument("first", first, "Int")(encoder1),
        Argument("last", last, "Int")(encoder2),
        Argument("before", before, "String")(encoder3),
        Argument("after", after, "String")(encoder4)
      )
    )

  /** Retrieve details of a user based on username
    */
  def internalUserByUsername[A](username: String)(innerSelection: SelectionBuilder[User, A])(
      implicit encoder0: ArgEncoder[String]
  ): SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "internalUserByUsername",
      OptionOf(Obj(innerSelection)),
      arguments = List(Argument("username", username, "String!")(encoder0))
    )

  /** Retrieve details of a user based on unique ID
    */
  def internalUserById[A](id: String)(innerSelection: SelectionBuilder[User, A])(
      implicit
      encoder0: ArgEncoder[String])
    : SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "internalUserById",
      OptionOf(Obj(innerSelection)),
      arguments = List(Argument("id", id, "String!")(encoder0))
    )
}
