package io.github.openequella.graphql.client

import caliban.client.FieldBuilder._
import caliban.client._

object UserEdge {
  def cursor: SelectionBuilder[UserEdge, String] =
    _root_.caliban.client.SelectionBuilder.Field("cursor", Scalar())
  def node[A](innerSelection: SelectionBuilder[User, A]): SelectionBuilder[UserEdge, A] =
    _root_.caliban.client.SelectionBuilder.Field("node", Obj(innerSelection))
}
