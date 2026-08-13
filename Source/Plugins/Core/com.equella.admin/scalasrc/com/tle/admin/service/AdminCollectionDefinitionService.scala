/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.admin.service

import com.tle.beans.entity.BaseEntityLabel
import com.tle.beans.entity.itemdef.ItemDefinition
import com.tle.core.remoting.RemoteAbstractEntityService

import java.util

trait AdminCollectionDefinitionService extends RemoteAbstractEntityService[ItemDefinition] {
  def enumerateCategories: util.List[String]

  def listUsableItemDefinitionsForSchema(schemaID: Long): util.List[BaseEntityLabel]

  def getSchemaIdForCollectionUuid(value: String): Long

  /** Packages a wizard control's XML definition as a `.wzc` zip file for saving locally. A purely
    * local operation — no server communication is involved.
    *
    * @param controlXml
    *   the XML definition of the control (treated as opaque).
    * @return
    *   the bytes of the `.wzc` zip file.
    * @see
    *   [[importControl]] for the inverse operation.
    */
  def exportControl(controlXml: String): Array[Byte]

  /** Extracts a wizard control's XML definition from a `.wzc` zip file. A purely local operation —
    * no server communication is involved.
    *
    * @param zipFileData
    *   the bytes of a `.wzc` zip file.
    * @return
    *   the XML definition of the control stored in the zip.
    * @see
    *   [[exportControl]] for the inverse operation.
    */
  def importControl(zipFileData: Array[Byte]): String
}

object AdminCollectionDefinitionService {
  val ENTITY_TYPE: String     = "COLLECTION"
  val ATTRIBUTE_KEY_FILESTORE = "filestore.location"
}
