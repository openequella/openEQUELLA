/**
  * Created by jolz on 26/04/17.
  */

import java.nio.file.Paths

import fs2._
import org.http4s._
import org.http4s.dsl._
import org.http4s.server.staticcontent._
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.staticcontent.FileService.Config
import org.http4s.util.StreamApp

object IntegTester extends StreamApp {
  val basePath = Paths.get("ps/dist").toAbsolutePath.toString
  def main(args: List[String]): fs2.Stream[Task, Unit] =
    BlazeBuilder
      .bindHttp(8083, "0.0.0.0")
      .mountService(fileService(Config(basePath)))
      .serve
}

