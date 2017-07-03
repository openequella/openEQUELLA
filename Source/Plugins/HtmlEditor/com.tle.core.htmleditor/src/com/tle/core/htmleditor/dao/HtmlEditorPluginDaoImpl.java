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

package com.tle.core.htmleditor.dao;

import javax.inject.Singleton;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.tle.common.htmleditor.beans.HtmlEditorPlugin;
import com.tle.core.entity.dao.impl.AbstractEntityDaoImpl;
import com.tle.core.guice.Bind;
import com.tle.common.institution.CurrentInstitution;

/**
 * @author aholland
 */
@Singleton
@SuppressWarnings("nls")
@Bind(HtmlEditorPluginDao.class)
public class HtmlEditorPluginDaoImpl extends AbstractEntityDaoImpl<HtmlEditorPlugin> implements HtmlEditorPluginDao
{
	public HtmlEditorPluginDaoImpl()
	{
		super(HtmlEditorPlugin.class);
	}

	@Override
	public HtmlEditorPlugin getByPluginId(String pluginId)
	{
		return findByCriteria(Restrictions.eq("pluginId", pluginId),
			Restrictions.eq("institution", CurrentInstitution.get()));
	}

	@Override
	public void changeUserId(final String fromUserId, final String toUserId)
	{
		getHibernateTemplate().execute(new TLEHibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException
			{
				final Query query = session.createQuery("UPDATE HtmlEditorPlugin SET userId = :toUserId"
					+ " WHERE userId = :fromUserId AND institution = :institution");
				query.setParameter("toUserId", toUserId);
				query.setParameter("fromUserId", fromUserId);
				query.setParameter("institution", CurrentInstitution.get());
				return query.executeUpdate();
			}
		});
	}
}
