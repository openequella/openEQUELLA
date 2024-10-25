package io.github.openequella.graphql.client

import caliban.client.FieldBuilder._
import caliban.client._

object PageInfo {
  def hasNextPage: SelectionBuilder[PageInfo, Boolean] =
    _root_.caliban.client.SelectionBuilder.Field("hasNextPage", Scalar())
  def hasPreviousPage: SelectionBuilder[PageInfo, Boolean] =
    _root_.caliban.client.SelectionBuilder.Field("hasPreviousPage", Scalar())
  def startCursor: SelectionBuilder[PageInfo, scala.Option[String]] =
    _root_.caliban.client.SelectionBuilder.Field("startCursor", OptionOf(Scalar()))
  def endCursor: SelectionBuilder[PageInfo, scala.Option[String]] =
    _root_.caliban.client.SelectionBuilder.Field("endCursor", OptionOf(Scalar()))
}
