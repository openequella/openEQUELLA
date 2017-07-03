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

import java.util.List;

import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.Item;
import com.tle.common.beans.exception.NotFoundException;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.qti.entity.QtiAssessmentTest;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericInstitionalDaoImpl;
import com.tle.core.qti.dao.QtiAssessmentTestDao;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
@NonNullByDefault
@Bind(QtiAssessmentTestDao.class)
@Singleton
public class QtiAssessmentTestDaoImpl extends GenericInstitionalDaoImpl<QtiAssessmentTest, Long>
	implements
		QtiAssessmentTestDao
{
	public QtiAssessmentTestDaoImpl()
	{
		super(QtiAssessmentTest.class);
	}

	@Override
	public QtiAssessmentTest getByUuid(String uuid)
	{
		final QtiAssessmentTest test = findByUuid(uuid);
		if( test == null )
		{
			throw new NotFoundException("Cannot find test with uuid " + uuid);
		}
		return test;
	}

	@Nullable
	@Override
	public QtiAssessmentTest findByUuid(String uuid)
	{
		final QtiAssessmentTest test = findByCriteria(Restrictions.eq("institution", CurrentInstitution.get()),
			Restrictions.eq("uuid", uuid));
		return test;
	}

	@Override
	public QtiAssessmentTest findByItem(Item item)
	{
		final QtiAssessmentTest test = findByCriteria(Restrictions.eq("item", item));
		return test; // NOSONAR (keeping local var for readability)
	}

	@Override
	public QtiAssessmentTest findByItemId(long itemId)
	{
		final QtiAssessmentTest test = findByCriteria(Restrictions.eq("item.id", itemId));
		return test; // NOSONAR (keeping local var for readability)
	}

	@SuppressWarnings("unchecked")
	private List<QtiAssessmentTest> listAll()
	{
		return getHibernateTemplate().find("FROM QtiAssessmentTest WHERE institution = ?",
			new Object[]{CurrentInstitution.get()});
	}

	@Transactional(propagation = Propagation.MANDATORY)
	@Override
	public void deleteAll()
	{
		for( QtiAssessmentTest test : listAll() )
		{
			delete(test);
			flush();
			clear();
		}
	}
}
