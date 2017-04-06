package com.tle.core.hibernate.dao;

import javax.inject.Inject;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.tle.core.hibernate.HibernateService;

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
