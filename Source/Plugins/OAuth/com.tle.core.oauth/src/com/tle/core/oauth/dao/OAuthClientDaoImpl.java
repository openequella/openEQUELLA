package com.tle.core.oauth.dao;

import javax.inject.Singleton;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.tle.common.oauth.beans.OAuthClient;
import com.tle.core.dao.impl.AbstractEntityDaoImpl;
import com.tle.core.guice.Bind;
import com.tle.core.user.CurrentInstitution;

/**
 * @author aholland
 */
@Singleton
@SuppressWarnings("nls")
@Bind(OAuthClientDao.class)
public class OAuthClientDaoImpl extends AbstractEntityDaoImpl<OAuthClient> implements OAuthClientDao
{
	public OAuthClientDaoImpl()
	{
		super(OAuthClient.class);
	}

	@Override
	public OAuthClient getByClientIdAndRedirectUrl(String clientId, String redirectUrl)
	{
		return findByCriteria(Restrictions.eq("clientId", clientId), Restrictions.eq("redirectUrl", redirectUrl),
			Restrictions.eq("institution", CurrentInstitution.get()));
	}

	@Override
	public OAuthClient getByClientIdOnly(String clientId)
	{
		return findByCriteria(Restrictions.eq("clientId", clientId),
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
				final Query query = session.createQuery("UPDATE OAuthClient SET userId = :toUserId"
					+ " WHERE userId = :fromUserId AND institution = :institution");
				query.setParameter("toUserId", toUserId);
				query.setParameter("fromUserId", fromUserId);
				query.setParameter("institution", CurrentInstitution.get());
				return query.executeUpdate();
			}
		});
	}
}
