package com.tle.web.controls.universal.handlers.fileupload

sealed trait PackageType
object PackageType {
  def fromString(s: String): PackageType = s match {
    case "IMS" => IMSPackage
    case "SCORM" => SCORMPackage
    case "QTITEST" => QTIPackage
    case o => OtherPackage(o)
  }
  def packageTypeString(pt: PackageType) : String = pt match {
    case IMSPackage => "IMS"
    case SCORMPackage => "SCORM"
    case QTIPackage => "QTITEST"
    case OtherPackage(o) => o
  }
}
case object IMSPackage extends PackageType
case object SCORMPackage extends PackageType
case object QTIPackage extends PackageType
case class OtherPackage(other: String) extends PackageType
