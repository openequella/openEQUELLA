package equellatests

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import io.circe.Decoder
import io.circe.parser.parse
import org.openqa.selenium.remote.Command

import reflect.runtime._

object ReplayTestCase {

  def main(args: Array[String]): Unit = {
    val fileContents = Files.readAllBytes(Paths.get(args(0)))
    parse(new String(fileContents, StandardCharsets.UTF_8)).flatMap(_.as[FailedTestCase]).flatMap { ftc =>
      val modSymbol = currentMirror.staticModule(ftc.propertiesClass)
      val propInst = currentMirror.reflectModule(modSymbol).instance.asInstanceOf[StatefulProperties]
      Decoder.decodeSeq(propInst.testCaseDecoder).decodeJson(ftc.testCase).map(tc => propInst.executeProp(ftc.shortName, tc,
        Some(ftc.failedAfter)).check)
    }.fold(throw _, _ => ())
  }
}
