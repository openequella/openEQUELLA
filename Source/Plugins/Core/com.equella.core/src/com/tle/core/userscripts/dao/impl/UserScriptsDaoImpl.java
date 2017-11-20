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

package com.tle.core.userscripts.dao.impl;

import java.sql.SQLException;
import java.util.List;

import javax.inject.Singleton;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.tle.common.institution.CurrentInstitution;
import com.tle.common.userscripts.UserScriptsConstants.ScriptTypes;
import com.tle.common.userscripts.entity.UserScript;
import com.tle.core.entity.dao.impl.AbstractEntityDaoImpl;
import com.tle.core.guice.Bind;
import com.tle.core.userscripts.dao.UserScriptsDao;

@Bind(UserScriptsDao.class)
@Singleton
@SuppressWarnings("nls")
public class UserScriptsDaoImpl extends AbstractEntityDaoImpl<UserScript> implements UserScriptsDao
{
	public UserScriptsDaoImpl()
	{
		super(UserScript.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserScript> enumerateForType(final ScriptTypes type)
	{
		return (List<UserScript>) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException
			{
				String query = "From UserScript WHERE scriptType LIKE :type AND disabled = :disabled AND institution = :inst";
				final Query q = session.createQuery(query).setParameter("type", type.toString())
					.setParameter("disabled", false).setParameter("inst", CurrentInstitution.get());
				return q.list();
			}
		});
	}

	@Override
	public boolean isModuleNameExist(final String moduleName, final long id)
	{
		return (boolean) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException
			{
				String query = "From UserScript WHERE moduleName = :moduleName AND institution = :inst AND scriptType LIKE :type AND id != :id";
				final Query q = session.createQuery(query).setParameter("moduleName", moduleName.toString())
					.setParameter("inst", CurrentInstitution.get()).setParameter("type", "EXECUTABLE")
					.setParameter("id", id);

				if( q.list().size() > 0 )
				{
					return true;
				}
				return false;
			}
		});
	}

}
