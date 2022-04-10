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

package com.tle.core.viewcount.service;

import com.tle.beans.Institution;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.ItemKey;
import com.tle.beans.viewcount.AbstractViewcount;
import com.tle.beans.viewcount.ViewcountAttachment;
import com.tle.beans.viewcount.ViewcountAttachmentId;
import com.tle.beans.viewcount.ViewcountItem;
import com.tle.beans.viewcount.ViewcountItemId;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.guice.Bind;
import com.tle.core.viewcount.dao.AttachmentViewCountDao;
import com.tle.core.viewcount.dao.ItemViewCountDao;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

@Bind(ViewCountService.class)
@Singleton
public class ViewCountServiceImpl implements ViewCountService {
  @Inject private ItemViewCountDao itemViewCountDao;
  @Inject private AttachmentViewCountDao attachmentViewCountDao;

  private ViewcountItemId buildViewCountItemId(ItemKey itemKey) {
    return new ViewcountItemId(
        CurrentInstitution.get().getDatabaseId(), itemKey.getUuid(), itemKey.getVersion());
  }

  private ViewcountAttachmentId buildViewCountAttachmentId(ItemKey itemKey, String attachmentUuid) {
    return new ViewcountAttachmentId(
        CurrentInstitution.get().getDatabaseId(),
        itemKey.getUuid(),
        itemKey.getVersion(),
        attachmentUuid);
  }

  private Criterion restrictedByInstitution(Institution institution) {
    return Restrictions.eq("id.inst", institution.getDatabaseId());
  }

  // If the given ViewCount exists, plus the current view count by 1, update the last viewed
  // time and return it.
  private <T extends AbstractViewcount> Optional<T> updateIfExists(T viewCount) {
    return Optional.ofNullable(viewCount)
        .map(
            vc -> {
              vc.setCount(vc.getCount() + 1);
              vc.setLastViewed(Instant.now());
              return vc;
            });
  }

  // If the given ViewCount exists, return the count. Otherwise, return 0.
  private int getViewCount(AbstractViewcount viewCount) {
    return Optional.ofNullable(viewCount).map(AbstractViewcount::getCount).orElse(0);
  }

  @Override
  @Transactional
  public void setItemViewCount(ItemKey itemKey, int viewCount, Instant lastViewed) {
    ViewcountItemId id = buildViewCountItemId(itemKey);
    itemViewCountDao.saveOrUpdate(new ViewcountItem(id, viewCount, lastViewed));
  }

  @Override
  @Transactional
  public void setAttachmentViewCount(
      ItemKey itemKey, String attachmentUuid, int viewCount, Instant lastViewed) {
    ViewcountAttachmentId id = buildViewCountAttachmentId(itemKey, attachmentUuid);
    attachmentViewCountDao.saveOrUpdate(new ViewcountAttachment(id, viewCount, lastViewed));
  }

  @Override
  public int incrementItemViewCount(ItemKey itemKey) {
    ViewcountItemId id = buildViewCountItemId(itemKey);

    ViewcountItem viewcountItem =
        updateIfExists(itemViewCountDao.findById(id))
            .orElse(new ViewcountItem(id, 1, Instant.now()));

    itemViewCountDao.saveOrUpdate(viewcountItem);
    return viewcountItem.getCount();
  }

  @Override
  public int incrementAttachmentViewCount(ItemKey itemKey, String attachmentUuid) {
    ViewcountAttachmentId id = buildViewCountAttachmentId(itemKey, attachmentUuid);

    ViewcountAttachment viewcountAttachment =
        updateIfExists(attachmentViewCountDao.findById(id))
            .orElse(new ViewcountAttachment(id, 1, Instant.now()));

    attachmentViewCountDao.saveOrUpdate(viewcountAttachment);

    return viewcountAttachment.getCount();
  }

  @Override
  public int getItemViewCount(ItemKey itemKey) {
    return getViewCount(itemViewCountDao.findById(buildViewCountItemId(itemKey)));
  }

  @Override
  public int getAttachmentViewCount(ItemKey itemKey, String attachmentUuid) {
    return getViewCount(
        attachmentViewCountDao.findById(buildViewCountAttachmentId(itemKey, attachmentUuid)));
  }

  @Override
  public List<ViewcountItem> getItemViewCountList(Institution institution) {
    return itemViewCountDao.findAllByCriteria(restrictedByInstitution(institution));
  }

  @Override
  public List<ViewcountAttachment> getAttachmentViewCountList(
      Institution institution, ItemKey itemKey) {
    return attachmentViewCountDao.findAllByCriteria(
        restrictedByInstitution(institution),
        Restrictions.eq("id.itemVersion", itemKey.getVersion()),
        Restrictions.eq("id.itemUuid", itemKey.getUuid()));
  }

  @Override
  public int getItemViewCountForCollection(ItemDefinition col) {
    return itemViewCountDao.getItemCountForCollection(col.getId());
  }

  @Override
  public int getAttachmentViewCountForCollection(ItemDefinition col) {
    return attachmentViewCountDao.getAttachmentViewCountForCollection(col.getId());
  }

  @Override
  @Transactional
  public void deleteViewCount(Institution institution, ItemKey itemKey) {
    itemViewCountDao.deleteItemViewCount(institution, itemKey);
    attachmentViewCountDao.deleteAttachmentViewCountForItem(institution, itemKey);
  }
}
