package equellatests.tests

import java.util.UUID

import equellatests._
import equellatests.browserpage.BrowserPage
import equellatests.domain._
import equellatests.instgen.fiveo._
import equellatests.pages.viewitem.SummaryPage
import equellatests.pages.wizard._
import equellatests.restapi.{BasicAttachment, ERest, RItems}
import equellatests.sections.wizard.{
  AttachmentEditPage,
  FileAttachmentEditPage,
  PackageAttachmentEditPage,
  WizardPageTab
}
import io.circe.generic.auto._
import io.circe.generic.semiauto._
import org.openqa.selenium.support.ui.ExpectedConditions
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Prop._
import org.scalacheck.{Gen, Prop}

object WizardFileUploadsProperties
    extends StatefulProperties("Wizard file uploads")
    with SimpleTestCase {
  type State   = FileUploadState
  type Command = FileUploadCommand

  object EditTypes extends Enumeration {
    val Description, Viewer, Restriction, Thumb = Value
  }

  object FailureTypes extends Enumeration {
    val Banned, WrongType, TooLarge = Value
  }

  case class FileUniversalControl(
      num: Int,
      canRestrict: Boolean,
      canSuppress: Boolean,
      defaultSuppressed: Boolean,
      noUnzip: Boolean = false,
      maximumAttachments: Int = Int.MaxValue,
      mimeTypes: Option[Set[String]] = None,
      maxFileSizeMB: Option[Int] = None
  ) {

    def fileCanAchieve(
        scenario: Either[EditTypes.Value, FailureTypes.Value]
    )(tf: TestFile): Boolean = scenario match {
      case Left(ed) if failureType(tf).isEmpty => matchesEdit(ed)(tf)
      case Right(fail)                         => matchesFailure(fail)(tf)
      case _                                   => false
    }

    def existsAnyFile(f: TestFile => Boolean): Boolean = TestFile.testFiles.exists(f)

    def filesForScenario(scenario: Either[EditTypes.Value, FailureTypes.Value]): Seq[TestFile] =
      TestFile.testFiles.filter(fileCanAchieve(scenario))

    def matchesEdit(ed: EditTypes.Value)(tf: TestFile): Boolean = ed match {
      case EditTypes.Restriction => canRestrict
      case EditTypes.Thumb       => canSuppress
      case EditTypes.Description => true
      case EditTypes.Viewer      => StandardMimeTypes.viewersForFile(tf).size > 1
    }

    def matchesFailure(failure: FailureTypes.Value)(tf: TestFile): Boolean = failure match {
      case FailureTypes.TooLarge => maxFileSizeMB.exists(_.toLong * 1024L * 1024L < tf.fileSize)
      case FailureTypes.WrongType =>
        mimeTypes.exists(!_.contains(StandardMimeTypes.extMimeMapping(tf.extension)))
      case FailureTypes.Banned => TestFile.bannedExt(tf.extension)
    }

    def illegalReason: PartialFunction[TestFile, String => String] = {
      case tf if matchesFailure(FailureTypes.TooLarge)(tf) =>
        fn =>
          s"This file cannot be uploaded because it is larger than the maximum file size allowed."
      case tf if mimeTypes.fold(false)(!_.apply(StandardMimeTypes.extMimeMapping(tf.extension))) =>
        fn =>
          s"""This control is restricted to certain file types. "$fn" is not allowed to be uploaded."""
      case tf if TestFile.bannedExt(tf.extension) =>
        fn => s"$fn: File upload cancelled.  File extension has been banned"
    }

    def failureType(tf: TestFile): Option[FailureTypes.Value] =
      FailureTypes.values.find(matchesFailure(_)(tf))

  }

  case class Attachment(
      id: UUID,
      control: FileUniversalControl,
      filename: String,
      description: String,
      restricted: Boolean,
      suppressThumb: Boolean,
      file: TestFile,
      viewer: Option[String] = None
  ) {
    def nameInTable: String =
      if (restricted) description + " (hidden from summary view)" else description

    lazy val details = StandardMimeTypes.commonDetailsForFile(file, filename, description)

    def ispackage = file.ispackage

    def viewerOptions: Option[Set[String]] =
      Option(StandardMimeTypes.viewersForFile(file)).filter(_.size > 1)
  }

  case class Item(
      itemId: Option[ItemId],
      name: String,
      wizard: String,
      attachments: Seq[Attachment]
  ) {
    def attachmentForId(attachUuid: UUID): Attachment = attachments.find(_.id == attachUuid).get

    def addAttachment(a: Attachment): Item = copy(attachments = attachments :+ a)

    def updateAttachment(a: Attachment): Item =
      copy(attachments = attachments.updated(attachments.indexWhere(_.id == a.id), a))
  }

  case class FileUploadState(
      savedItem: Option[Item] = None,
      savedAgain: Boolean = false,
      currentPage: Location = LoginPageLoc,
      unverified: Set[UUID] = Set.empty,
      edits: EditTypes.ValueSet = EditTypes.ValueSet.empty,
      failures: FailureTypes.ValueSet = FailureTypes.ValueSet.empty
  )

  sealed trait Location

  case object SummaryPageLoc extends Location

  case object LoginPageLoc extends Location

  case class Page1(editingItem: Item) extends Location

  case class AttachmentDetailsPage(
      original: Attachment,
      edited: Attachment,
      control: FileUniversalControl,
      page1: Page1
  ) extends Location

  object AttachmentDetailsPage {
    def collectDetails(a: AttachmentEditPage): AttachmentDetails =
      AttachmentDetails(
        a.description,
        a.restricted,
        a.suppressThumb,
        a.details().toSet,
        a.viewerOptions
      )

  }

  case class AttachmentDetails(
      description: String,
      restricted: Option[Boolean],
      suppressThumb: Option[Boolean],
      details: Set[(String, String)],
      viewers: Option[Set[String]]
  )

  sealed abstract class AttachmentEdit(val editType: EditTypes.Value) {
    def edit: Attachment => Attachment

    def run: AttachmentEditPage => Unit

    def label: String

    override def toString = label
  }

  case class DescriptionEdit(nd: String) extends AttachmentEdit(EditTypes.Description) {
    def edit = _.copy(description = nd)

    def run = _.description = nd

    def label = s"Editing description to $nd"
  }

  case class RestrictionEdit(nr: Boolean) extends AttachmentEdit(EditTypes.Restriction) {
    def edit = _.copy(restricted = nr)

    def run = _.restricted = nr

    def label = s"Changing restricted to $nr"
  }

  case class ThumbSettingEdit(ns: Boolean) extends AttachmentEdit(EditTypes.Thumb) {
    def edit = _.copy(suppressThumb = ns)

    def run = _.suppressThumb = ns

    def label = s"Changing suppressThumb to $ns"
  }

  case class ViewerEdit(viewer: Option[String]) extends AttachmentEdit(EditTypes.Viewer) {
    def edit = _.copy(viewer = viewer)

    def run = _.viewer = viewer

    def label = s"Changing viewer to $viewer"
  }

  sealed trait FileUploadCommand

  case object EditItem extends FileUploadCommand

  case class CreateItem(name: String, wizard: String) extends FileUploadCommand

  case class UploadInlineFile(
      tf: TestFile,
      filename: String,
      controlIndex: Int,
      id: UUID,
      failed: Boolean
  ) extends FileUploadCommand

  case class StartEditingAttachment(attachUuid: UUID) extends FileUploadCommand

  case object CloseEditDialog extends FileUploadCommand

  case class EditAttachmentDetails(changes: AttachmentEdit) extends FileUploadCommand

  case object SaveAttachment extends FileUploadCommand

  case object SaveItem extends FileUploadCommand

  val testCaseDecoder = deriveDecoder[FileUploadCommand]
  val testCaseEncoder = deriveEncoder[FileUploadCommand]

  type AttachmentEditGen =
    (Attachment, FileUniversalControl, Seq[Attachment]) => Option[Gen[AttachmentEdit]]

  val wizards = Seq(
    "Navigation and Attachments",
    "Attachment mimetype restriction collection",
    "Attachment filesize restriction collection"
  )

  val ctrlsForWizard: Map[String, Seq[FileUniversalControl]] = Map(
    "Navigation and Attachments" -> Seq(
      FileUniversalControl(2, canRestrict = true, canSuppress = false, defaultSuppressed = false),
      FileUniversalControl(
        3,
        canRestrict = true,
        canSuppress = false,
        defaultSuppressed = false,
        maximumAttachments = 1
      )
    ),
    "Attachment mimetype restriction collection" -> Seq(
      FileUniversalControl(
        2,
        canRestrict = true,
        canSuppress = false,
        defaultSuppressed = false,
        maximumAttachments = 1,
        mimeTypes = Some(Set("image/jpeg"))
      )
    ),
    "Attachment filesize restriction collection" -> Seq(
      FileUniversalControl(
        2,
        canRestrict = true,
        canSuppress = false,
        defaultSuppressed = false,
        maximumAttachments = 1,
        maxFileSizeMB = Some(1)
      )
    )
  )

  def ctrlIndex(item: Item, fuc: FileUniversalControl): Int =
    ctrlsForWizard(item.wizard).indexOf(fuc)

  def fucFromIndex(item: Item, index: Int): FileUniversalControl =
    ctrlsForWizard(item.wizard).apply(index)

  def ctrlsThatCanAdd(item: Item) =
    ctrlsForWizard(item.wizard).filter(fuc =>
      item.attachments.count(_.control == fuc) < fuc.maximumAttachments
    )

  def canAddMore(item: Item): Boolean = ctrlsThatCanAdd(item).nonEmpty

  def attachmentEditFor(
      editType: EditTypes.Value,
      a: Attachment,
      others: Seq[Attachment]
  ): Gen[AttachmentEdit] = editType match {
    case EditTypes.Description =>
      arbitrary[ValidDescription].map(vd =>
        DescriptionEdit(Uniqueify.uniqueify(ValidDescription.addNumber)(others.contains, vd).desc)
      )
    case EditTypes.Restriction => RestrictionEdit(!a.restricted)
    case EditTypes.Viewer =>
      Gen.oneOf(a.viewerOptions.get.toSeq :+ "").map(v => ViewerEdit(Some(v).filterNot(_.isEmpty)))
    case EditTypes.Thumb => ThumbSettingEdit(!a.suppressThumb)
  }

  def attachmentCanBeEditedBy(editType: EditTypes.Value)(a: Attachment): Boolean =
    a.control.matchesEdit(editType)(a.file)

  def availableEdits(item: Item): EditTypes.ValueSet = {
    val attachments = item.attachments
    EditTypes.values.filter(e => attachments.exists(attachmentCanBeEditedBy(e)))
  }

  def availableFailures(item: Item): FailureTypes.ValueSet = {
    FailureTypes.values.filter(ft =>
      ctrlsThatCanAdd(item).exists(fuc => fuc.existsAnyFile(fuc.matchesFailure(ft)))
    )
  }

  def availableEdits(a: Attachment): EditTypes.ValueSet = {
    EditTypes.values.filter(e => attachmentCanBeEditedBy(e)(a))
  }

  def possibleFailures(
      remaining: FailureTypes.ValueSet
  )(controls: Seq[FileUniversalControl]): FailureTypes.ValueSet = {
    remaining.filter(ft => controls.exists(c => c.existsAnyFile(c.matchesFailure(ft))))
  }

  def possibleEdits(
      remaining: EditTypes.ValueSet
  )(controls: Seq[FileUniversalControl]): EditTypes.ValueSet = {
    remaining.filter(ed => controls.exists(c => c.existsAnyFile(c.matchesEdit(ed))))
  }

  statefulProp("edit details") {
    val finishedSet =
      wizards.map(w => possibleEdits(EditTypes.values)(ctrlsForWizard(w))).reduce(_ ++ _)
    val finishedFailures =
      wizards.map(w => possibleFailures(FailureTypes.values)(ctrlsForWizard(w))).reduce(_ ++ _)
    def createNewItem =
      for {
        name   <- arbitrary[UniqueRandomWord]
        wizard <- Gen.oneOf(wizards)
      } yield List(CreateItem(name.word, wizard))
    generateCommands { s =>
      val remainingEdits    = finishedSet -- s.edits
      val remainingFailures = finishedFailures -- s.failures
      if (
        s.savedItem.isDefined && s.unverified.isEmpty &&
        remainingEdits.isEmpty && remainingFailures.isEmpty
      ) List()
      else
        commandsWith(s.currentPage) {
          case LoginPageLoc   => createNewItem
          case SummaryPageLoc => if (s.unverified.nonEmpty) List(EditItem) else createNewItem
          case Page1(item) =>
            val edits       = availableEdits(item)
            val failures    = availableFailures(item)
            val newEdits    = edits -- s.edits
            val newFailures = failures -- s.failures
            if (newEdits.nonEmpty) {
              for {
                a <- Gen.oneOf(
                  item.attachments.filter(a => newEdits.exists(e => attachmentCanBeEditedBy(e)(a)))
                )
              } yield List(StartEditingAttachment(a.id))
            } else {
              val addable = ctrlsThatCanAdd(item)
              val pEdits  = possibleEdits(remainingEdits)(addable)
              val pErrors = possibleFailures(newFailures)(addable)
              val allScen = pEdits.toSeq.map(Left(_)) ++ pErrors.toSeq.map(Right(_))
              if (allScen.nonEmpty) {
                for {
                  scen <- Gen.oneOf(allScen)
                  fuc  <- Gen.oneOf(addable.filter(c => c.existsAnyFile(c.fileCanAchieve(scen))))
                  tf   <- Gen.oneOf(fuc.filesForScenario(scen))
                  vfn  <- arbitrary[ValidFilename]
                  actualFilename = s"${vfn.filename}.${tf.extension}"
                } yield List(
                  UploadInlineFile(
                    tf,
                    actualFilename,
                    ctrlIndex(item, fuc),
                    UUID.randomUUID(),
                    scen.isRight
                  )
                )
              } else if (s.unverified.isEmpty) List(SaveItem)
              else
                for {
                  a <- Gen.oneOf(s.unverified.toSeq)
                } yield List(StartEditingAttachment(a))
            }
          case AttachmentDetailsPage(a, edited, control, Page1(item)) => {
            val edits    = availableEdits(edited)
            val newEdits = edits -- s.edits
            if (newEdits.isEmpty) List(SaveAttachment)
            else
              for {
                editType <- Gen.oneOf(newEdits.toSeq)
                editCommand <- attachmentEditFor(
                  editType,
                  edited,
                  item.attachments.filterNot(_.id == a.id)
                )
              } yield List(EditAttachmentDetails(editCommand))
          }
        }
    }
  }

  override def initialState = FileUploadState()

  override def runCommand(c: FileUploadCommand, s: FileUploadState) = {
    def withCurrentPage(pf: PartialFunction[Location, FileUploadState]): FileUploadState = {
      pf.apply(s.currentPage)
    }

    c match {
      case EditItem => s.savedItem.map(item => s.copy(currentPage = Page1(item))).getOrElse(s)
      case CreateItem(name, wizard) =>
        s.copy(
          savedItem = None,
          savedAgain = false,
          currentPage = Page1(Item(None, name, wizard, Seq.empty))
        )

      case UploadInlineFile(tf, filename, controlIndex, id, failed) =>
        withCurrentPage { case p1 @ Page1(item) =>
          val control = fucFromIndex(item, controlIndex)
          if (!failed) {
            val desc = tf.packageName.getOrElse(filename)
            val attach = Attachment(
              id,
              control,
              filename,
              desc,
              restricted = false,
              suppressThumb = control.defaultSuppressed,
              tf
            )
            s.copy(currentPage = p1.copy(editingItem = item.addAttachment(attach)))
          } else s.copy(failures = s.failures ++ control.failureType(tf))
        }
      case StartEditingAttachment(attachUuid) =>
        withCurrentPage { case p1 @ Page1(item) =>
          val a = item.attachmentForId(attachUuid)
          s.copy(
            currentPage = AttachmentDetailsPage(a, a, a.control, p1),
            unverified = s.unverified - a.id
          )
        }
      case CloseEditDialog =>
        withCurrentPage { case AttachmentDetailsPage(_, a, _, page1) =>
          s.copy(currentPage = page1)
        }
      case EditAttachmentDetails(changes) =>
        withCurrentPage { case adp: AttachmentDetailsPage =>
          s.copy(
            currentPage = adp.copy(edited = changes.edit(adp.edited)),
            unverified = s.unverified + adp.edited.id,
            edits = s.edits + changes.editType
          )
        }
      case SaveAttachment =>
        withCurrentPage { case AttachmentDetailsPage(_, a, _, page1) =>
          s.copy(currentPage = page1.copy(page1.editingItem.updateAttachment(a)))
        }
      case SaveItem =>
        withCurrentPage { case Page1(item) =>
          s.copy(
            savedItem = Some(item),
            savedAgain = s.savedItem.isDefined,
            currentPage = SummaryPageLoc
          )
        }
    }
  }

  def compareDetails(ad: AttachmentDetails, attachment: Attachment): Prop = {
    val uc = attachment.control
    all(
      (ad.description ?= attachment.description) :| "description",
      if (uc.canRestrict) (ad.restricted ?= Some(attachment.restricted)) :| "restricted flag"
      else Prop(ad.restricted.isEmpty) :| "no restrict flag",
      (ad.details.filterNot(_._1 == "Views:") ?= attachment.details) :| "details",
      (ad.viewers ?= attachment.viewerOptions) :| "viewers",
      if (uc.canSuppress && !attachment.ispackage)
        (ad.suppressThumb ?= Some(attachment.suppressThumb)) :| "thumb supression flag"
      else Prop(ad.suppressThumb.isEmpty) :| "no suppress flag"
    ).label(attachment.description)
  }

  def compareBasicDetails(ba: BasicAttachment, attachment: Attachment): Prop = {
    val uc = attachment.control
    all(
      (ba.description ?= attachment.description) :| "description",
      (ba.restricted ?= attachment.restricted) :| "restricted flag",
      (ba.viewerO ?= attachment.viewer) :| "viewer",
      (if (attachment.suppressThumb) ba.thumbnail ?= Some("none")
       else Prop.proved) :| "suppressThumb"
    ).label(attachment.description)
  }

  override def runCommandInBrowser(
      c: FileUploadCommand,
      s: FileUploadState,
      b: SimpleSeleniumBrowser
  ) = c match {
    case EditItem => b.runOnPage { case sp: SummaryPage => sp.edit() }
    case CreateItem(name, wizard) =>
      b.run {
        ContributePage(b.page.ctx).load().openWizard(wizard)
      }
    case UploadInlineFile(tf, filename, controlIndex, id, failed) =>
      b.verifyOnPageAndState(s.currentPage) { case (Page1(item), page1: WizardPageTab) =>
        val control             = fucFromIndex(item, controlIndex)
        val ctrl                = page1.universalControl(control.num)
        val expectedDescription = tf.packageName.getOrElse(filename)
        val failure             = control.illegalReason.lift(tf).map(_(filename))
        val w = failure
          .map(ctrl.errorExpectation)
          .getOrElse(ctrl.attachNameWaiter(expectedDescription, false))
        ctrl.uploadInline(tf, filename, w)
        if (failure.isDefined) {
          ctrl.cancelUpload(filename)
        }
        (page1, true)
      }
    case StartEditingAttachment(attachUuid) =>
      b.verifyOnPageAndState(s.currentPage) { case (Page1(item), page1: WizardPageTab) =>
        val a  = item.attachmentForId(attachUuid)
        val uc = page1.universalControl(a.control.num)
        val waitPage =
          if (a.ispackage) new PackageAttachmentEditPage(uc) else new FileAttachmentEditPage(uc)
        uc.editResource(a.nameInTable, waitPage.pageExpectation)
          .map { edPage =>
            edPage -> compareDetails(AttachmentDetailsPage.collectDetails(edPage), a)
          }
          .getOrElse {
            page1 -> Prop.falsified.label(s"No attachment in table called '${a.nameInTable}'")
          }
      }
    case CloseEditDialog => b.runOnPage { case fae: AttachmentEditPage => fae.close() }
    case EditAttachmentDetails(changes) =>
      b.runOnPage { case aep: AttachmentEditPage => changes.run(aep); aep }
    case SaveAttachment => b.runOnPage { case aep: AttachmentEditPage => aep.save() }
    case SaveItem =>
      b.verifyOnPageAndState(s.currentPage) { case (Page1(item), page1: WizardPageTab) =>
        val summary = if (s.savedItem.isDefined) page1.saveToSummary() else page1.save().publish()
        val itemId  = summary.itemId()
        val attachDetails = ERest.run(page1.ctx) {
          for {
            ritem <- RItems.get(itemId)
          } yield item.attachments
            .map(a => (a, ritem.attachments.find(_.description == a.description)))
            .map {
              case (a, Some(ba)) => compareBasicDetails(ba, a)
              case (a, _)        => Prop.falsified.label(s"Missing attachment: $a")
            }
        }
        (summary, all(attachDetails: _*))
      }
  }

  override def logon = autoTestLogon
}
