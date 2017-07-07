import com.yahoo.platform.yui.compressor.{CssCompressor, JavaScriptCompressor, YUICompressor}
import org.mozilla.javascript.{ErrorReporter, EvaluatorException}
import sbt._
import sbt.Keys._
import sbt.plugins.JvmPlugin

object YUICompressPlugin extends AutoPlugin {

  object autoImport {
    lazy val yuiResources = taskKey[Seq[File]]("Resources to minify")
  }

  override def requires: Plugins = JvmPlugin

  override def trigger = noTrigger

  import autoImport._

  object jsReporter extends ErrorReporter {
    def runtimeError(s: String, s1: String, i: Int, s2: String, i1: Int): EvaluatorException = ???

    def error(s: String, s1: String, i: Int, s2: String, i1: Int): Unit = ???

    def warning(s: String, s1: String, i: Int, s2: String, i1: Int): Unit = ???
  }

  override def projectSettings: Seq[Def.Setting[_]] = {
    resourceGenerators in Compile += Def.task {
      val baseDir = (resourceManaged in Compile).value
      yuiResources.value.pair(rebase((resourceDirectory in Compile).value, "")).map {
        case (f, path) => IO.reader(f) { br =>
          val ind = path.lastIndexOf('.')
          path.substring(ind + 1) match {
            case "js" =>
              val jsCompress = new JavaScriptCompressor(br, jsReporter)
              val outFile = baseDir / (path.substring(0, ind) + ".min.js")
              IO.writer(outFile, "", IO.utf8) { bw =>
                jsCompress.compress(bw, 8000, true, false, false, false)
              }
              outFile
            case "css" =>
              val cssCompress = new CssCompressor(br)
              val outFile = baseDir / (path.substring(0, ind) + ".min.css")
              IO.writer(outFile, "", IO.utf8) { bw =>
                cssCompress.compress(bw, 8000)
              }
              outFile
          }
        }
      }
    }.taskValue
  }
}
