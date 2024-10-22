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

package com.tle.core.hierarchy.impl;

import com.google.common.base.Preconditions;
import com.tle.beans.Institution;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.PowerSearch;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.hierarchy.HierarchyTopic;
import com.tle.beans.hierarchy.HierarchyTopicKeyResource;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.dao.impl.AbstractTreeDaoImpl;
import com.tle.core.guice.Bind;
import com.tle.core.hierarchy.HierarchyDao;
import com.tle.core.i18n.dao.LanguageDao;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;

/** @author Nicholas Read */
@Bind(HierarchyDao.class)
@Singleton
@SuppressWarnings("nls")
public class HierarchyDaoImpl extends AbstractTreeDaoImpl<HierarchyTopic> implements HierarchyDao {
  @Inject private LanguageDao languageDao;

  public HierarchyDaoImpl() {
    super(HierarchyTopic.class);
  }

  @Override
  public LanguageBundle getHierarchyTopicName(final long topicID) {
    return (LanguageBundle)
        getHibernateTemplate()
            .execute(
                new HibernateCallback() {
                  @Override
                  public Object doInHibernate(Session session) throws HibernateException {
                    Query query =
                        session.createQuery("select name from HierarchyTopic where id = :id");
                    query.setParameter("id", topicID);
                    query.setCacheable(true);

                    Iterator<?> iter = query.iterate();
                    if (iter.hasNext()) {
                      return iter.next();
                    } else {
                      return null;
                    }
                  }
                });
  }

  @Override
  @Transactional
  public void deleteInOrder(Long node) {
    HierarchyTopic topic = findById(node);
    List<Long> bundleIds = new ArrayList<Long>();
    addBundleId(topic.getName(), bundleIds);
    addBundleId(topic.getShortDescription(), bundleIds);
    addBundleId(topic.getLongDescription(), bundleIds);
    addBundleId(topic.getSubtopicsSectionName(), bundleIds);
    addBundleId(topic.getResultsSectionName(), bundleIds);
    delete(topic);
    flush();
    languageDao.deleteBundles(bundleIds);
  }

  private void addBundleId(LanguageBundle bundle, List<Long> bundleIds) {
    if (bundle != null) {
      bundleIds.add(bundle.getId());
    }
  }

  /*
   * (non-Javadoc)
   * @see com.tle.core.dao.HierarchyDao#enumerateAll()
   */
  @Override
  public List<HierarchyTopic> enumerateAll() {
    return (List<HierarchyTopic>)
        getHibernateTemplate()
            .find("from HierarchyTopic where institution = ?0", CurrentInstitution.get());
  }

  /*
   * (non-Javadoc)
   * @see
   * com.tle.core.dao.HierarchyDao#getTopicsReferencingItemDefinition(com.
   * tle.beans.entity.itemdef.ItemDefinition)
   */
  @Override
  public Collection<HierarchyTopic> getTopicsReferencingItemDefinition(
      ItemDefinition itemDefinition) {
    return getTopicsReferencingEntity(itemDefinition, "Idefs");
  }

  /*
   * (non-Javadoc)
   * @see
   * com.tle.core.dao.HierarchyDao#getTopicsReferencingSchema(com.tle.beans
   * .entity.Schema)
   */
  @Override
  public Collection<HierarchyTopic> getTopicsReferencingSchema(Schema schema) {
    return getTopicsReferencingEntity(schema, "Schemas");
  }

  @SuppressWarnings("unchecked")
  private Collection<HierarchyTopic> getTopicsReferencingEntity(
      BaseEntity entity, String entityPostfix) {
    List list1 =
        getHibernateTemplate()
            .findByNamedParam(
                "from HierarchyTopic t join t.inh" + entityPostfix + " a where a.entity = :entity",
                "entity",
                entity);
    List list2 =
        getHibernateTemplate()
            .findByNamedParam(
                "from HierarchyTopic t join t.add" + entityPostfix + " a where a.entity = :entity",
                "entity",
                entity);
    HashSet<HierarchyTopic> topics = new HashSet<HierarchyTopic>();
    topics.addAll(list1);
    topics.addAll(list2);
    return topics;
  }

  /*
   * (non-Javadoc)
   * @see
   * com.tle.core.dao.HierarchyDao#getTopicsReferencingPowerSearch(com.tle
   * .beans.entity.PowerSearch)
   */
  @Override
  public List<HierarchyTopic> getTopicsReferencingPowerSearch(PowerSearch powerSearch) {
    return (List<HierarchyTopic>)
        getHibernateTemplate()
            .findByNamedParam(
                "from HierarchyTopic t where advancedSearch = :powerSearch",
                "powerSearch",
                powerSearch);
  }

  @Override
  public HierarchyTopic findByUuid(String uuid, Institution institution) {
    Preconditions.checkNotNull(uuid);
    List<HierarchyTopic> topics =
        (List<HierarchyTopic>)
            getHibernateTemplate()
                .find(
                    "from HierarchyTopic t WHERE uuid = ?0 AND institution = ?1",
                    new Object[] {uuid, institution});
    if (topics.size() == 0) {
      return null;
    }
    return topics.get(0);
  }

  @Override
  public void deleteKeyResources(String itemUuid, int itemVersion, Institution institution) {
    getHibernateTemplate()
        .bulkUpdate(
            "delete from HierarchyTopicKeyResource WHERE itemUuid = ?0 AND itemVersion =?1 "
                + "AND institution = ?2",
            new Object[] {itemUuid, itemVersion, institution});
  }

  @Override
  public void deleteKeyResource(String topicId, String itemUuid, int itemVersion) {
    getHibernateTemplate()
        .bulkUpdate(
            "delete from HierarchyTopicKeyResource WHERE "
                + "hierarchyCompoundUuid = ?0 AND itemUuid = ?1 AND itemVersion =?2",
            new Object[] {topicId, itemUuid, itemVersion});
  }

  @Override
  public void deleteAllKeyResources(Institution institution) {
    List<HierarchyTopicKeyResource> keyResources = getAllKeyResources(institution);
    for (HierarchyTopicKeyResource key : keyResources) {
      getHibernateTemplate().delete(key);
    }
  }

  @Override
  public void saveKeyResource(HierarchyTopicKeyResource entity) {
    getHibernateTemplate().save(entity);
  }

  @Override
  public void deleteKeyResource(HierarchyTopicKeyResource entity) {
    getHibernateTemplate().delete(entity);
  }

  @Override
  public List<HierarchyTopicKeyResource> getKeyResources(
      String dynamicHierarchyId, Institution institution) {
    List<HierarchyTopicKeyResource> keyResources =
        (List<HierarchyTopicKeyResource>)
            getHibernateTemplate()
                .find(
                    "from HierarchyTopicKeyResource t WHERE hierarchyCompoundUuid = ?0 AND institution = ?1",
                    new Object[] {dynamicHierarchyId, institution});
    return keyResources;
  }

  @Override
  public Optional<HierarchyTopicKeyResource> getKeyResource(
      String legacyHierarchyCompoundUuid,
      String itemUuid,
      int itemVersion,
      Institution institution) {
    List<HierarchyTopicKeyResource> keyResources =
        (List<HierarchyTopicKeyResource>)
            getHibernateTemplate()
                .find(
                    "from HierarchyTopicKeyResource t WHERE hierarchyCompoundUuid = ?0 AND itemUuid = ?1 "
                        + "AND itemVersion =?2 AND institution = ?3",
                    new Object[] {legacyHierarchyCompoundUuid, itemUuid, itemVersion, institution});
    return keyResources.isEmpty() ? Optional.empty() : Optional.of(keyResources.getFirst());
  }

  @Override
  public List<HierarchyTopicKeyResource> getKeyResources(
      String itemUuid, int itemVersion, Institution institution) {

    List<HierarchyTopicKeyResource> keyResources =
        (List<HierarchyTopicKeyResource>)
            getHibernateTemplate()
                .find(
                    "from HierarchyTopicKeyResource t WHERE itemUuid = ?0 AND itemVersion =?1 AND  institution = ?2",
                    new Object[] {itemUuid, itemVersion, institution});
    return keyResources;
  }

  @Override
  public List<HierarchyTopicKeyResource> getKeyResourcesByItemUuid(
      String itemUuid, Institution institution) {
    return (List<HierarchyTopicKeyResource>)
        getHibernateTemplate()
            .execute(
                (Session session) -> {
                  return session
                      .getNamedQuery("getByItemUuidAndInstitution")
                      .setParameter("itemUuid", itemUuid)
                      .setParameter("institution", CurrentInstitution.get())
                      .list();
                });
  }

  @Override
  public List<HierarchyTopicKeyResource> getAllKeyResources(Institution institution) {
    List<HierarchyTopicKeyResource> keyResources =
        (List<HierarchyTopicKeyResource>)
            getHibernateTemplate()
                .find(
                    "from HierarchyTopicKeyResource t WHERE institution =?0",
                    new Object[] {institution});
    return keyResources;
  }
}
