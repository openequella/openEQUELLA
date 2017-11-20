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

package com.tle.web.sections.equella

import com.tle.common.Utils
import com.tle.web.sections.{Section, SectionInfo, SectionTree}

abstract class AbstractScalaSection extends Section {
  type M <: AnyRef

  var treeRegisteredIn : SectionTree = _
  var sectionId : String = _

  def getModel(info: SectionInfo): M = info.getModelForId(getSectionId)
  def newModel: SectionInfo => M

  override def instantiateModel(info: SectionInfo): AnyRef = newModel(info)

  override def getTree: SectionTree = treeRegisteredIn

  override def getSectionId: String = sectionId

  override def registered(id: String, tree: SectionTree): Unit = {
    sectionId = id
    treeRegisteredIn = tree
  }

  override def isTreeIndexed: Boolean = true

  override def treeFinished(id: String, tree: SectionTree): Unit = {}

  override def getSectionObject: Section = this

  override def getDefaultPropertyName: String = {
    val _className = getClass.getSimpleName
    val className = if (_className.endsWith("Section")) _className.substring(0, _className.length - 7) else _className
    val caps: Array[String] = className.split("[a-z0-9]*")
    caps.mkString("").toLowerCase
  }

}