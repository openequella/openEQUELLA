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

package com.tle.core.qti.dao.impl;

import javax.inject.Singleton;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.beans.exception.NotFoundException;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.qti.entity.QtiAssessmentItemRef;
import com.tle.common.qti.entity.QtiAssessmentTest;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericInstitionalDaoImpl;
import com.tle.core.qti.dao.QtiAssessmentItemRefDao;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
@NonNullByDefault
@Bind(QtiAssessmentItemRefDao.class)
@Singleton
public class QtiAssessmentItemRefDaoImpl extends GenericInstitionalDaoImpl<QtiAssessmentItemRef, Long>
	implements
		QtiAssessmentItemRefDao
{
	public QtiAssessmentItemRefDaoImpl()
	{
		super(QtiAssessmentItemRef.class);
	}

	@Override
	public QtiAssessmentItemRef getByUuid(final String uuid)
	{
		final QtiAssessmentItemRef question = (QtiAssessmentItemRef) getHibernateTemplate()
			.execute(new TLEHibernateCallback()
			{
				@Override
				public Object doInHibernate(Session session) throws HibernateException
				{
					final Criteria criteria = createCriteria(session).createAlias("test", "t")
						.add(Restrictions.eq("t.institution", CurrentInstitution.get()))
						.add(Restrictions.eq("uuid", uuid));
					return criteria.uniqueResult();
				}
			});
		if( question == null )
		{
			throw new NotFoundException("Cannot find assessment item with uuid " + uuid);
		}
		return question;
	}

	@Override
	public QtiAssessmentItemRef getByIdentifier(QtiAssessmentTest test, String identifier)
	{
		final QtiAssessmentItemRef question = findByCriteria(Restrictions.eq("test", test),
			Restrictions.eq("identifier", identifier));
		if( question == null )
		{
			throw new NotFoundException(
				"Cannot find assessment item with identifier " + identifier + " in test " + test.getUuid());
		}
		return question;
	}
}
