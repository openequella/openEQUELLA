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

package com.tle.core.services.item.relation;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.Relation;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericInstitionalDaoImpl;
import com.tle.core.item.dao.ItemDaoExtension;
import java.util.Collection;
import java.util.List;
import javax.inject.Singleton;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Bind(RelationDao.class)
@Singleton
public class RelationDaoImpl extends GenericInstitionalDaoImpl<Relation, Long>
    implements RelationDao, ItemDaoExtension {
  public RelationDaoImpl() {
    super(Relation.class);
  }

  @Override
  public Collection<Relation> getAllByFromItem(Item from) {
    String query = "from Relation where firstItem = ?0"; // $NON-NLS-1$
    return (Collection<Relation>) getHibernateTemplate().find(query, from);
  }

  @Override
  public Collection<Relation> getAllByToItem(Item to) {
    String query = "from Relation where secondItem = ?0"; // $NON-NLS-1$
    return (Collection<Relation>) getHibernateTemplate().find(query, to);
  }

  @Override
  public Collection<Relation> getAllByType(String type) {
    String query = "from Relation where relationType = ?0"; // $NON-NLS-1$
    return (Collection<Relation>) getHibernateTemplate().find(query, type);
  }

  @Override
  public Collection<Relation> getAllByFromItemAndType(Item from, String type) {
    String query = "from Relation where firstItem = ?0 and relationType = ?1"; // $NON-NLS-1$
    return (Collection<Relation>) getHibernateTemplate().find(query, new Object[] {from, type});
  }

  @Override
  public Collection<Relation> getAllByToItemAndType(Item to, String type) {
    String query = "from Relation where secondItem = ?0 and relationType = ?1"; // $NON-NLS-1$
    return (Collection<Relation>) getHibernateTemplate().find(query, new Object[] {to, type});
  }

  @Override
  public Collection<Long> getAllIdsForInstitution() {
    String query = "select r.id from Relation r where r.firstItem.institution = ?0"; // $NON-NLS-1$
    return (Collection<Long>)
        getHibernateTemplate().find(query, new Object[] {CurrentInstitution.get()});
  }

  @Override
  public Collection<Relation> getAllMentioningItem(Item item) {
    String query = "from Relation where firstItem = ?0 or secondItem = ?1"; // $NON-NLS-1$
    return (Collection<Relation>) getHibernateTemplate().find(query, new Object[] {item, item});
  }

  @Override
  public void delete(Item item) {
    String query = "delete from Relation where firstItem = ?0 or secondItem = ?1"; // $NON-NLS-1$
    getHibernateTemplate().bulkUpdate(query, new Object[] {item, item});
  }

  @SuppressWarnings("nls")
  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public List<Relation> getAllMentioningItem(ItemKey itemId) {
    String query =
        "from Relation where (firstItem.uuid = :uuid and firstItem.version = :version) or"
            + " (secondItem.uuid = :uuid and secondItem.version = :version)";
    return (List<Relation>)
        getHibernateTemplate()
            .findByNamedParam(
                query,
                new String[] {"uuid", "version"},
                new Object[] {itemId.getUuid(), itemId.getVersion()});
  }
}
