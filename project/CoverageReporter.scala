import org.jacoco.core.analysis.{Analyzer, CoverageBuilder}
import org.jacoco.core.tools.ExecFileLoader
import org.jacoco.report.{DirectorySourceFileLocator, FileMultiReportOutput}
import org.jacoco.report.html.HTMLFormatter
import sbt._

object CoverageReporter {


  def createReport(execFile: File, allClasses: File, title: String, outDir: File, sourceDir: File) = {
    val loader = new ExecFileLoader()
    loader.load(execFile)
    val coverageBuilder = new CoverageBuilder
    val analyzer = new Analyzer(loader.getExecutionDataStore, coverageBuilder)
    analyzer.analyzeAll(allClasses)
    val bundleCoverage = coverageBuilder.getBundle(title)
    val htmlFormatter = new HTMLFormatter
    val visitor = htmlFormatter.createVisitor(new FileMultiReportOutput(outDir))
    visitor.visitInfo(loader.getSessionInfoStore.getInfos, loader.getExecutionDataStore.getContents)
    visitor.visitBundle(bundleCoverage, new DirectorySourceFileLocator(sourceDir, "utf-8", 4))
    visitor.visitEnd()
  }
}
