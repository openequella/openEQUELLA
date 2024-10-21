package io.github.openequella.graphql.client

import caliban.client.FieldBuilder._
import caliban.client._

object UserConnection {
  def pageInfo[A](
      innerSelection: SelectionBuilder[PageInfo, A]
  ): SelectionBuilder[UserConnection, A] =
    _root_.caliban.client.SelectionBuilder.Field("pageInfo", Obj(innerSelection))
  def edges[A](
      innerSelection: SelectionBuilder[UserEdge, A]
  ): SelectionBuilder[UserConnection, List[A]] =
    _root_.caliban.client.SelectionBuilder.Field("edges", ListOf(Obj(innerSelection)))
}
