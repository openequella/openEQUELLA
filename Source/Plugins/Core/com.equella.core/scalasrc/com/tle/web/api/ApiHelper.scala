package com.tle.web.api

import cats.data.OptionT
import com.tle.core.db.{DB, RunWithDB}
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.{ResponseBuilder, Status}

object ApiHelper {
  def runAndBuild(db: DB[ResponseBuilder]): Response = RunWithDB.execute(db.map(_.build()))

  def entityOrNotFound[A](o: Option[A]): ResponseBuilder =
    o.fold(Response.status(Status.NOT_FOUND))(Response.ok(_))


  def entityOrNotFoundDB[A](db: OptionT[DB, A]): DB[ResponseBuilder] =
    db.value.map(entityOrNotFound)

}
