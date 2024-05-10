package com.tle.web.remoting.graphql.schema

import caliban.relay.{Base64Cursor, Cursor, Pagination, PaginationArgs, PaginationCursor}
import org.scalatest.GivenWhenThen
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks._
import io.github.openequella.ziohelpers._

case class TestPaginationArgs(first: Option[Int] = None,
                              last: Option[Int] = None,
                              before: Option[String] = None,
                              after: Option[String] = None)
    extends PaginationArgs[Base64Cursor] {
  override def toString: String = {
    def decodeCursor(cursor: String): String = Cursor[Base64Cursor].decode(cursor) match {
      case Left(_)      => "Bad cursor!!"
      case Right(value) => value.toString
    }

    val firstStr     = first.map(f => s"first=$f").getOrElse("")
    val lastStr      = last.map(l => s"last=$l").getOrElse("")
    val beforeStr    = before.map(b => s"before=${decodeCursor(b)}").getOrElse("")
    val afterStr     = after.map(a => s"after=${decodeCursor(a)}").getOrElse("")
    val paramSummary = Seq(firstStr, lastStr, beforeStr, afterStr).filter(_.nonEmpty).mkString(", ")
    s"TestPaginationArgs($paramSummary)"
  }
}

class PaginationTest extends AnyFunSpec with Matchers with GivenWhenThen {
  // Helper to side step the ZIO error handling, as it's irrelevant to the tests
  private def paginationOrThrow(args: TestPaginationArgs): Pagination[Base64Cursor] = {
    zioToEither[Pagination[Base64Cursor]](Pagination(args)) match {
      case Left(value)  => throw new IllegalArgumentException(value.toString)
      case Right(value) => value
    }
  }

  // Helper to easily generate a string representation of a cursor, just as would be used in our API
  private def cursor(value: Int): String = Cursor[Base64Cursor].encode(Base64Cursor(value))

  describe("paginationOffsetLimit") {
    // Make sure that the values are correctly calculated for the offset and limit, based on
    // [GraphQL Cursor Connections Specification - 4.4 Pagination Algorithm](https://relay.dev/graphql/connections.htm#sec-Pagination-algorithm)
    it("should return the correct offset and limit") {
      val tenItems = Some(10)
      val item     = (i: Int) => Some(cursor(i))

      // The table below assumes a `max` value of 100 - i.e. 100 items in the database
      val paginationsFor100Items = Table(
        ("pagination", "offset", "limit"),
        (TestPaginationArgs(first = tenItems), 0, 10),
        (TestPaginationArgs(first = tenItems, after = item(10)), 11, 10),
        (TestPaginationArgs(first = tenItems, before = item(10)), 0, 10),
        (TestPaginationArgs(last = tenItems), 90, 10),
        (TestPaginationArgs(last = tenItems, before = item(90)), 80, 10),
        // boundary tests
        (TestPaginationArgs(first = tenItems, after = item(99)), 100, 0),
        (TestPaginationArgs(first = tenItems, before = item(5)), 0, 5),
        (TestPaginationArgs(last = tenItems, after = item(90)), 91, 9),
        (TestPaginationArgs(last = tenItems, before = item(0)), 0, 0),
      )

      forAll(paginationsFor100Items) { (pagination, offset, limit) =>
        val (o, l) = paginationOffsetLimit(paginationOrThrow(pagination), 100)
        o shouldBe offset
        l shouldBe limit
      }
    }
  }
}
