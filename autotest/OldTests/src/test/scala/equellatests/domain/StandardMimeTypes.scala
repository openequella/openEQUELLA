package equellatests.domain

import com.tle.common.{FileSizeUtils, PathUtils}

case class FileWithViewers(file: TestFile, viewers: Iterable[String])

object StandardMimeTypes {

  val extMimeMapping = Map(
    "jpg"  -> "image/jpeg",
    "jpeg" -> "image/jpeg",
    "dng"  -> "image/dng",
    "png"  -> "image/png",
    "ico"  -> "image/icon",
    "xml"  -> "text/xml",
    "txt"  -> "text/plain",
    "html" -> "text/html",
    "pdf"  -> "application/pdf",
    "zip"  -> "application/zip",
    "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation",
    "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
  ).withDefaultValue("application/octet-stream")

  val webImageViewers   = List("fancy", "livNavTreeViewer")
  val otherImageViewers = List("livNavTreeViewer")
  val mimeViewerMapping = Map(
    "image/jpeg" -> webImageViewers,
    "image/png"  -> webImageViewers,
    "image/dng"  -> otherImageViewers,
    "image/icon" -> otherImageViewers
  ).withDefaultValue(List())

  val friendlyType: String => String = {
    case i if i.startsWith("image/") => "Image"
    case "text/plain"                => "Text Document"
    case "text/html"                 => "HTML Document"
    case "application/pdf"           => "PDF Document"
    case "application/zip"           => "Archive"
    case o                           => o
  }

  def viewersForFile(tf: TestFile): Set[String] = {
    tf.packageType.map(TestFile.viewersFor).getOrElse {
      mimeViewerMapping(extMimeMapping(tf.extension)).toSet
    } + "file"
  }

  def commonDetailsForFile(
      tf: TestFile,
      filename: String,
      description: String
  ): Set[(String, String)] = {
    def first30(l: String) = if (l.length > 30) l.substring(0, 30) + "..." else l

    val dtype = first30(
      tf.packageType.getOrElse(friendlyType(extMimeMapping(PathUtils.extension(filename))))
    )
    val fsize      = FileSizeUtils.humanReadableFileSize(tf.fileSize)
    val nameDetail = if (tf.ispackage) "Name:" -> description else "Filename:" -> filename
    Set(
      "Type:" -> dtype,
      nameDetail,
      (if (tf.packageType.contains(TestFile.qtiTestType)) "Package size:" else "Size:") -> fsize
    ) ++ tf.extraDetails
  }
}
