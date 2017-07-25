import org.jacoco.core.analysis.{Analyzer, CoverageBuilder}
import org.jacoco.core.tools.ExecFileLoader
import org.jacoco.report.{DirectorySourceFileLocator, FileMultiReportOutput}
import org.jacoco.report.html.HTMLFormatter
import sbt._

case class CoveragePlugin(classes: File, name: String)

object CoverageReporter {


  def createReport(loader: ExecFileLoader, groups: Seq[(String, Seq[CoveragePlugin])], outDir: File, sourceDir: File) = {
    val htmlFormatter = new HTMLFormatter
    val visitor = htmlFormatter.createVisitor(new FileMultiReportOutput(outDir))
    visitor.visitInfo(loader.getSessionInfoStore.getInfos, loader.getExecutionDataStore.getContents)
    val topGroup = visitor.visitGroup("EQUELLA")
    groups.foreach {
      case (g, plugins) =>
        val v = topGroup.visitGroup(g)
        plugins.foreach { p =>
          val coverageBuilder = new CoverageBuilder
          val analyzer = new Analyzer(loader.getExecutionDataStore, coverageBuilder)
          analyzer.analyzeAll(p.classes)
          val bundleCoverage = coverageBuilder.getBundle(p.name)
          v.visitBundle(bundleCoverage, new DirectorySourceFileLocator(sourceDir, "utf-8", 4))
        }
    }
    visitor.visitEnd()
  }
}
