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

package com.tle.core.workflow.thumbnail.dao;

import java.sql.SQLException;
import java.util.List;

import javax.inject.Singleton;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.Institution;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericInstitionalDaoImpl;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.workflow.thumbnail.entity.ThumbnailRequest;

/**
 * @author Aaron
 *
 */
@SuppressWarnings("nls")
@NonNullByDefault
@Bind(ThumbnailRequestDao.class)
@Singleton
public class ThumbnailRequestDaoImpl extends GenericInstitionalDaoImpl<ThumbnailRequest, Long>
	implements
		ThumbnailRequestDao
{
	public ThumbnailRequestDaoImpl()
	{
		super(ThumbnailRequest.class);
	}

	@Transactional
	@Override
	public List<ThumbnailRequest> list(Institution institution)
	{
		return findAllByCriteria(Order.desc("dateRequested"), -1, Restrictions.eq("institution", institution));
	}

	@Transactional
	@Override
	public List<ThumbnailRequest> list(Institution institution, ItemKey itemId)
	{
		return findAllByCriteria(Order.desc("dateRequested"), -1, Restrictions.eq("institution", institution),
			Restrictions.eq("itemUuid", itemId.getUuid()), Restrictions.eq("itemVersion", itemId.getVersion()));
	}

	@Transactional
	@Override
	public List<ThumbnailRequest> listForFile(Institution institution, ItemKey itemId, String filenameHash)
	{
		return findAllByCriteria(Order.desc("dateRequested"), -1, Restrictions.eq("institution", institution),
			Restrictions.eq("itemUuid", itemId.getUuid()), Restrictions.eq("itemVersion", itemId.getVersion()),
			Restrictions.eq("filenameHash", filenameHash));
	}

	@Transactional
	@Override
	public List<ThumbnailRequest> listForHandle(Institution institution, ItemKey itemId, String serialHandle)
	{
		return findAllByCriteria(Order.desc("dateRequested"), -1, Restrictions.eq("institution", institution),
			Restrictions.eq("itemUuid", itemId.getUuid()), Restrictions.eq("itemVersion", itemId.getVersion()),
			Restrictions.eq("handle", serialHandle));
	}

	@Nullable
	@Override
	public ThumbnailRequest getByUuid(String requestUuid)
	{
		return findByCriteria(Restrictions.eq("uuid", requestUuid),
			Restrictions.eq("institution", CurrentInstitution.get()));
	}

	@Override
	public boolean exists(ItemKey itemId, String serialHandle, String filename, String filenameHash)
	{
		return (boolean) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException
			{
				Query query = session
					.createQuery(
						"select filename from ThumbnailRequest "
							+ "where institution = :institution and itemUuid = :itemUuid"
							+ " and itemVersion = :itemVersion and handle = :serialHandle and filenameHash = :filenameHash")
					.setParameter("institution", CurrentInstitution.get()).setParameter("itemUuid", itemId.getUuid())
					.setParameter("itemVersion", itemId.getVersion()).setParameter("serialHandle", serialHandle)
					.setParameter("filenameHash", filenameHash);
				List<String> list = query.list();
				for( String fn : list )
				{
					if( fn.equals(filename) )
					{
						return true;
					}
				}
				return false;
			}
		});
	}

	@Transactional(propagation = Propagation.MANDATORY)
	@Override
	public void delete(Item item)
	{
		List<ThumbnailRequest> thumbsForItem = findAllByCriteria(Restrictions.eq("itemUuid", item.getUuid()),
			Restrictions.eq("itemVersion", item.getVersion()), Restrictions.eq("institution", CurrentInstitution.get()));
		for( ThumbnailRequest thumb : thumbsForItem )
		{
			delete(thumb);
		}
	}
}
