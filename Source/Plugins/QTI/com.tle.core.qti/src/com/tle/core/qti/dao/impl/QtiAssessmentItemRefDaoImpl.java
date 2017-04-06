package com.tle.core.qti.dao.impl;

import javax.inject.Singleton;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.dytech.edge.exceptions.NotFoundException;
import com.tle.annotation.NonNullByDefault;
import com.tle.common.qti.entity.QtiAssessmentItemRef;
import com.tle.common.qti.entity.QtiAssessmentTest;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericInstitionalDaoImpl;
import com.tle.core.qti.dao.QtiAssessmentItemRefDao;
import com.tle.core.user.CurrentInstitution;

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
		final QtiAssessmentItemRef question = (QtiAssessmentItemRef) getHibernateTemplate().execute(
			new TLEHibernateCallback()
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
			throw new NotFoundException("Cannot find assessment item with identifier " + identifier + " in test "
				+ test.getUuid());
		}
		return question;
	}
}
