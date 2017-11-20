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

package com.tle.web.workflow.notification

import javax.inject.Inject

import com.tle.beans.item.{Item, ItemId, ItemTaskId}
import com.tle.common.workflow.node.WorkflowNode
import com.tle.core.i18n.BundleCache
import com.tle.core.item.service.ItemService
import com.tle.core.notification.beans.Notification
import com.tle.core.services.user.LazyUserLookup
import com.tle.web.sections.SectionsController
import com.tle.web.sections.render.Label
import com.tle.web.sections.result.util.{BundleLabel, ItemNameLabel, UserLabel}
import com.tle.web.viewurl.ViewItemUrlFactory
import com.tle.web.workflow.tasks.RootTaskListSection

import scala.collection.JavaConverters._

trait NotificationLookup
{
  @Inject
  var itemService : ItemService = _

  @Inject
  var bundleCache : BundleCache = _

  @Inject
  var viewItemUrlFactory : ViewItemUrlFactory = _

  @Inject
  var controller : SectionsController = _

  trait ItemNotification extends NotificationModel
  {
    def note: Notification
    def item: Item
    def getItemName = new ItemNameLabel(item, bundleCache)
    def getLink = viewItemUrlFactory.createFullItemUrl(item.getItemId)
  }

  trait OwnerLookup extends ItemNotification
  {
    def lul: LazyUserLookup
    def getOwner = new UserLabel(item.getOwner, lul)
  }

  trait TaskNotification extends ItemNotification
  {
    val itemTaskId = new ItemTaskId(note.getItemid)
    def taskId = itemTaskId.getTaskId
    def getTaskName = taskLabel(taskId)
    def getTaskLink = linkToTask(taskId)
    def workflowItem(taskId: String) : Option[WorkflowNode] =
      Option(item.getItemDefinition.getWorkflow).flatMap(_.getNodes.asScala.find(_.getUuid == taskId))


    def taskLabel(taskId: String) : Label = {
      workflowItem(taskId).map { wn => new BundleLabel(wn.getName, bundleCache) }.getOrElse(NotificationLangStrings.unknownTask(taskId))
    }
    def linkToTask(taskId: String) = {
      RootTaskListSection.createModerateBookmark(controller, new ItemTaskId(item.getItemId, taskId))
    }
  }

  def createItemNotification(tName: String)(n: Notification, i: Item): ItemNotification =
    new ItemNotification {
      def item = i
      def note = n
      def group = StdNotificationGroup(tName, n.getReason)
    }

  def createItemNotifications(templateName: String, notifications: Iterable[Notification]): Iterable[ItemNotification] =
    createDataIgnore(notifications, createItemNotification(templateName)).toSeq.sortBy(_.getItemName.getText.toLowerCase)

  def createDataIgnore[A](notifications: Iterable[Notification], f: (Notification, Item) => A): Iterable[A] =
    createData(notifications, (n,oi) => oi.map(f(n, _)))

  protected def createData[A](notifications: Iterable[Notification], f: (Notification, Option[Item]) => Option[A]): Iterable[A] = {
    def itemOnly(n: Notification) : ItemId = new ItemId(n.getItemidOnly)
    val itemIds = notifications.toBuffer[Notification].map(itemOnly)
    val itemMap = itemService.queryItemsByItemIds(itemIds.asJava)
    notifications.flatMap { n => f(n, Option(itemMap.get(itemOnly(n)))) }
  }
}

