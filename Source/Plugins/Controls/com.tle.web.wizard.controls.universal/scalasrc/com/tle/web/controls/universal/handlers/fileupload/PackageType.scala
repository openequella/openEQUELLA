/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
