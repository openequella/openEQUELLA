import com.yahoo.platform.yui.compressor.{CssCompressor, JavaScriptCompressor, YUICompressor}
import org.mozilla.javascript.{ErrorReporter, EvaluatorException}
import sbt._
import sbt.Keys._
import sbt.plugins.JvmPlugin
import Path.rebase

object YUICompressPlugin extends AutoPlugin {

  object autoImport {
    lazy val yuiResources = taskKey[Seq[File]]("Resources to minify")
    lazy val minify       = taskKey[Seq[File]]("Generate minified resources")
  }

  override def requires: Plugins = JvmPlugin

  override def trigger = noTrigger

  import autoImport._

  object jsReporter extends ErrorReporter {
    def runtimeError(s: String, s1: String, i: Int, s2: String, i1: Int): EvaluatorException = ???

    def error(s: String, s1: String, i: Int, s2: String, i1: Int): Unit = ???

    def warning(s: String, s1: String, i: Int, s2: String, i1: Int): Unit = ???
  }

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    (Compile / minify) := {
      val logger  = streams.value.log
      val baseDir = (Compile / resourceManaged).value
      yuiResources.value.pair(rebase((Compile / resourceDirectory).value, "")).map {
        case (f, path) =>
          IO.reader(f) { br =>
            val ind = path.lastIndexOf('.')
            path.substring(ind + 1) match {
              case "js" =>
                val jsCompress = new JavaScriptCompressor(br, jsReporter)
                val outFile    = baseDir / (path.substring(0, ind) + ".min.js")
                logger.info(s"Minifying ${outFile.absolutePath}")
                IO.writer(outFile, "", IO.utf8) { bw =>
                  jsCompress.compress(bw, 8000, true, false, false, false)
                }
                outFile
              case "css" =>
                val cssCompress = new CssCompressor(br)
                val outFile     = baseDir / (path.substring(0, ind) + ".min.css")
                logger.info(s"Minifying ${outFile.absolutePath}")
                IO.writer(outFile, "", IO.utf8) { bw =>
                  cssCompress.compress(bw, 8000)
                }
                outFile
            }
          }
      }
    },
    (Compile / resourceGenerators) += (Compile / minify).taskValue
  )
}
