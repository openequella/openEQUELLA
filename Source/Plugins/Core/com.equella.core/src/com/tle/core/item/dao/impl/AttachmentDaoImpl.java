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

package com.tle.core.item.dao.impl;

import com.google.common.collect.Lists;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.common.Check;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericDaoImpl;
import com.tle.core.item.dao.AttachmentDao;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Singleton;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

@SuppressWarnings("nls")
@Bind(AttachmentDao.class)
@Singleton
public class AttachmentDaoImpl extends GenericDaoImpl<Attachment, Long> implements AttachmentDao {
  public AttachmentDaoImpl() {
    super(Attachment.class);
  }

  @Override
  public List<Attachment> findByMd5Sum(
      final String md5Sum,
      ItemDefinition collection,
      boolean ignoreDeletedRejectedSuspenedItems,
      String excludedItemUuid) {
    String[] names = new String[] {"md5sum", "collection", "institution"};
    Object[] values = new Object[] {md5Sum, collection, CurrentInstitution.get()};
    List<String> nameList = new ArrayList<String>(Arrays.asList(names));
    List<Object> valueList = new ArrayList<Object>(Arrays.asList(values));

    StringBuilder query = new StringBuilder();
    query.append("SELECT a FROM Item i LEFT JOIN i.attachments a WHERE a.md5sum = :md5sum");
    query.append(" AND i.itemDefinition = :collection AND i.institution = :institution");
    if (ignoreDeletedRejectedSuspenedItems) {
      query.append(" AND i.status NOT IN ('REJECTED', 'SUSPENDED', 'DELETED', 'ARCHIVED')");
    }
    if (excludedItemUuid != null) {
      query.append(" AND i.uuid != :excludedItemUuid");
      nameList.add("excludedItemUuid");
      valueList.add(excludedItemUuid);
    }
    List<Attachment> attachments =
        (List<Attachment>)
            getHibernateTemplate()
                .findByNamedParam(
                    query.toString(),
                    nameList.toArray(new String[nameList.size()]),
                    valueList.toArray());

    return attachments;
  }

  @Override
  public List<FileAttachment> findFilesWithNoMD5Sum() {
    String hql =
        "FROM FileAttachment WHERE (md5sum IS NULL OR md5sum = '') AND item.institution ="
            + " :institution";
    return (List<FileAttachment>)
        getHibernateTemplate().findByNamedParam(hql, "institution", CurrentInstitution.get());
  }

  @Override
  public List<CustomAttachment> findResourceAttachmentsByQuery(
      final String query, boolean liveOnly, String sortHql) {
    String q = query;
    String hql =
        "FROM CustomAttachment a WHERE a.item.institution = :institution AND a.value1 ="
            + " :resourcetype";

    final List<String> params = Lists.newArrayList();
    params.add("institution");
    params.add("resourcetype");

    final List<Object> paramVals = Lists.newArrayList();
    paramVals.add(CurrentInstitution.get());
    paramVals.add("resource");

    if (!Check.isEmpty(q)) {
      q = q.trim().toLowerCase();
      if (!q.startsWith("%")) {
        q = "%" + q;
      }
      if (!q.endsWith("%")) {
        q = q + "%";
      }
      hql += " AND LOWER(a.description) LIKE :query";
      params.add("query");
      paramVals.add(q);
    }
    if (liveOnly) {
      hql += " AND item.status = :status";
      params.add("status");
      paramVals.add(ItemStatus.LIVE.name());
    }
    hql += " " + sortHql;

    final List<CustomAttachment> attachments =
        (List<CustomAttachment>)
            getHibernateTemplate()
                .findByNamedParam(
                    hql, params.toArray(new String[params.size()]), paramVals.toArray());
    // it's possible that value1 could be
    return attachments;
  }

  // Criteria for checking attachments against institution of the item and UUID of the
  // attachment. Detached so that we don't need to create a Hibernate session until one is required.
  private DetachedCriteria criteriaByUuid(String uuid) {
    return DetachedCriteria.forClass(Attachment.class)
        .createAlias("item", "i")
        .add(Restrictions.eq("i.institution", CurrentInstitution.get()))
        .add(Restrictions.eq("uuid", uuid));
  }
}
