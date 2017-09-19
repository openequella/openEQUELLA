package com.tle.web.workflow.tasks.comments

import java.util.Date

import com.tle.common.workflow.WorkflowMessage
import com.tle.core.filesystem.WorkflowMessageFile
import com.tle.core.services.FileSystemService
import com.tle.web.freemarker.FreemarkerFactory
import com.tle.web.sections.SectionInfo
import com.tle.web.sections.equella.render.JQueryTimeAgo
import com.tle.web.sections.equella.utils.UserLinkSection
import com.tle.web.sections.render.{SectionRenderable, TextLabel}
import com.tle.web.sections.standard.model.{HtmlLinkState, SimpleBookmark}
import com.tle.web.workflow.servlet.WorkflowMessageServlet

import scala.collection.JavaConverters._

object ModCommentRender {

  def render(info: SectionInfo, viewFactory: FreemarkerFactory,
             userLinkSection: UserLinkSection, fileSystemService: FileSystemService,
             comments: java.util.Collection[WorkflowMessage]): SectionRenderable = {

    case class ModRow(wc: WorkflowMessage)
    {
      def getMessage = wc.getMessage
      def getExtraClass = wc.getType match {
        case WorkflowMessage.TYPE_ACCEPT => "approval"
        case WorkflowMessage.TYPE_REJECT => "rejection"
        case _ => ""
      }
      def getDateRenderer = JQueryTimeAgo.timeAgoTag(wc.getDate)
      def getUser = userLinkSection.createLink(info, wc.getUser)
      val getAttachments = {
        val wfile = new WorkflowMessageFile(wc.getUuid)
        fileSystemService.enumerate(wfile, "", null).map {
          fe => new HtmlLinkState(new TextLabel(fe.getName),
            new SimpleBookmark(WorkflowMessageServlet.messageUrl(wc.getUuid, fe.getName)))
        }
      }
    }

    viewFactory.createResultWithModel("modcomments.ftl",
      comments.asScala.toSeq.sortBy(_.getDate)(Ordering[Date].reverse).map(ModRow).asJava);
  }
}