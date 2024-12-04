import java.io.{IOException, InterruptedIOException}
import java.net.{InetAddress, Socket}

import org.jacoco.core.analysis.{Analyzer, CoverageBuilder}
import org.jacoco.core.runtime.{RemoteControlReader, RemoteControlWriter}
import org.jacoco.core.tools.ExecFileLoader
import org.jacoco.report.{DirectorySourceFileLocator, FileMultiReportOutput}
import org.jacoco.report.html.HTMLFormatter
import sbt._

import scala.annotation.tailrec

case class CoveragePlugin(classes: File, name: String)

object CoverageReporter {

  def createReport(
      loader: ExecFileLoader,
      groups: Seq[(String, Seq[CoveragePlugin])],
      outDir: File,
      sourceDir: File
  ) = {
    val htmlFormatter = new HTMLFormatter
    val visitor       = htmlFormatter.createVisitor(new FileMultiReportOutput(outDir))
    visitor.visitInfo(loader.getSessionInfoStore.getInfos, loader.getExecutionDataStore.getContents)
    val topGroup = visitor.visitGroup("openEQUELLA")
    groups.foreach { case (g, plugins) =>
      val v = topGroup.visitGroup(g)
      plugins.foreach { p =>
        val coverageBuilder = new CoverageBuilder
        val analyzer        = new Analyzer(loader.getExecutionDataStore, coverageBuilder)
        analyzer.analyzeAll(p.classes)
        val bundleCoverage = coverageBuilder.getBundle(p.name)
        v.visitBundle(bundleCoverage, new DirectorySourceFileLocator(sourceDir, "utf-8", 4))
      }
    }
    visitor.visitEnd()
  }

  def dumpCoverage(loader: ExecFileLoader, address: String, port: Int): Unit = {
    val socket = tryConnect(InetAddress.getByName(address), port, 5)
    try {
      val remoteWriter = new RemoteControlWriter(socket.getOutputStream)
      val remoteReader = new RemoteControlReader(socket.getInputStream)
      remoteReader.setSessionInfoVisitor(loader.getSessionInfoStore)
      remoteReader.setExecutionDataVisitor(loader.getExecutionDataStore)
      remoteWriter.visitDumpCommand(true, false)
      remoteReader.read
    } finally socket.close()
  }

  val retryDelay = 500

  @throws[IOException]
  @tailrec
  private def tryConnect(address: InetAddress, port: Int, tries: Int): Socket = {
    try {
      new Socket(address, port)
    } catch {
      case e: IOException =>
        sleep()
        if (tries < 1) sys.error("")
        tryConnect(address, port, tries - 1)
    }
  }

  private def sleep() = {
    try Thread.sleep(retryDelay)
    catch {
      case e: InterruptedException =>
        throw new InterruptedIOException
    }
  }
}
