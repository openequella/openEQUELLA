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

package com.tle.core.services.item.relation;

import java.util.Collection;
import java.util.List;

import javax.inject.Singleton;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.Relation;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericInstitionalDaoImpl;
import com.tle.core.item.dao.ItemDaoExtension;

@Bind(RelationDao.class)
@Singleton
public class RelationDaoImpl extends GenericInstitionalDaoImpl<Relation, Long> implements RelationDao, ItemDaoExtension
{
	public RelationDaoImpl()
	{
		super(Relation.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Relation> getAllByFromItem(Item from)
	{
		String query = "from Relation where firstItem = ?"; //$NON-NLS-1$
		return getHibernateTemplate().find(query, from);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Relation> getAllByToItem(Item to)
	{
		String query = "from Relation where secondItem = ?"; //$NON-NLS-1$
		return getHibernateTemplate().find(query, to);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Relation> getAllByType(String type)
	{
		String query = "from Relation where relationType = ?"; //$NON-NLS-1$
		return getHibernateTemplate().find(query, type);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Relation> getAllByFromItemAndType(Item from, String type)
	{
		String query = "from Relation where firstItem = ? and relationType = ?"; //$NON-NLS-1$
		return getHibernateTemplate().find(query, new Object[]{from, type});
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Relation> getAllByToItemAndType(Item to, String type)
	{
		String query = "from Relation where secondItem = ? and relationType = ?"; //$NON-NLS-1$
		return getHibernateTemplate().find(query, new Object[]{to, type});
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Long> getAllIdsForInstitution()
	{
		String query = "select r.id from Relation r where r.firstItem.institution = ?"; //$NON-NLS-1$
		return getHibernateTemplate().find(query, new Object[]{CurrentInstitution.get()});
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Relation> getAllMentioningItem(Item item)
	{
		String query = "from Relation where firstItem = ? or secondItem = ?"; //$NON-NLS-1$
		return getHibernateTemplate().find(query, new Object[]{item, item});
	}

	@Override
	public void delete(Item item)
	{
		String query = "delete from Relation where firstItem = ? or secondItem = ?"; //$NON-NLS-1$
		getHibernateTemplate().bulkUpdate(query, new Object[]{item, item});
	}

	@SuppressWarnings({"unchecked", "nls"})
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<Relation> getAllMentioningItem(ItemKey itemId)
	{
		String query = "from Relation where (firstItem.uuid = :uuid and firstItem.version = :version) or (secondItem.uuid = :uuid and secondItem.version = :version)";
		return getHibernateTemplate().findByNamedParam(query, new String[]{"uuid", "version"},
			new Object[]{itemId.getUuid(), itemId.getVersion()});
	}
}
