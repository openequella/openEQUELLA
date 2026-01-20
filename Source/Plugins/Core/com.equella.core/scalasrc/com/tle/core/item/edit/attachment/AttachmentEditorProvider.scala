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

package com.tle.core.item.edit.attachment

import com.tle.beans.item.ItemEditingException
import com.tle.core.cloudproviders.CloudAttachmentEditor
import com.tle.legacy.LegacyGuice

object AttachmentEditorProvider {

  private val tracker = LegacyGuice.attachEditorTracker

  def createEditorForType(className: String): AbstractAttachmentEditor = {
    val extMap = tracker.getExtensionMap
    Option(extMap.get(className)).map(tracker.getNewBeanByExtension).getOrElse {
      className match {
        case c if c == classOf[CloudAttachmentEditor].getName => new CloudAttachmentEditor()
        case _ =>
          throw new ItemEditingException(
            s"No extension for '$className' ${classOf[CloudAttachmentEditor].getName}"
          )
      }
    }
  }
}
