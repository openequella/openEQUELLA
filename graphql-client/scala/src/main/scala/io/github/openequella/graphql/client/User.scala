package io.github.openequella.graphql.client

import caliban.client.FieldBuilder._
import caliban.client._

object User {

  /** The unique identifier for the user
    */
  def uniqueId: SelectionBuilder[User, String] =
    _root_.caliban.client.SelectionBuilder.Field("uniqueId", Scalar())

  /** The username a user authenticates with
    */
  def username: SelectionBuilder[User, String] =
    _root_.caliban.client.SelectionBuilder.Field("username", Scalar())

  /** User's email address
    */
  def email: SelectionBuilder[User, scala.Option[String]] =
    _root_.caliban.client.SelectionBuilder.Field("email", OptionOf(Scalar()))

  /** User's first name
    */
  def firstName: SelectionBuilder[User, String] =
    _root_.caliban.client.SelectionBuilder.Field("firstName", Scalar())

  /** User's last name
    */
  def lastName: SelectionBuilder[User, String] =
    _root_.caliban.client.SelectionBuilder.Field("lastName", Scalar())
}
