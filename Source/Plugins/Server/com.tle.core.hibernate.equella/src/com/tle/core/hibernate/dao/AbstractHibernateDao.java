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

package com.tle.core.hibernate.dao;

import javax.inject.Inject;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.tle.annotation.NonNullByDefault;
import com.tle.core.hibernate.HibernateService;

@NonNullByDefault
public abstract class AbstractHibernateDao
{
	@Inject
	private HibernateService hibernateService;

	private SessionFactory lastFactory;

	private HibernateTemplate template;

	protected synchronized HibernateTemplate getHibernateTemplate()
	{
		SessionFactory newFactory = hibernateService.getTransactionAwareSessionFactory(getFactoryName(),
			isSystemDataSource());
		if( !newFactory.equals(lastFactory) )
		{
			lastFactory = newFactory;
			template = new HibernateTemplate(newFactory)
			{
				@Override
				protected Object doExecute(HibernateCallback action, boolean enforceNewSession,
					boolean enforceNativeSession) throws DataAccessException
				{
					Thread currentThread = Thread.currentThread();
					ClassLoader origLoader = currentThread.getContextClassLoader();
					try
					{
						currentThread.setContextClassLoader(Session.class.getClassLoader());
						return super.doExecute(action, enforceNewSession, enforceNativeSession);
					}
					finally
					{
						currentThread.setContextClassLoader(origLoader);
					}
				}
			};
			template.setAllowCreate(false);
			template.setExposeNativeSession(true);
		}
		return template;
	}

	protected boolean isSystemDataSource()
	{
		return false;
	}

	protected String getFactoryName()
	{
		return "main"; //$NON-NLS-1$
	}

}
