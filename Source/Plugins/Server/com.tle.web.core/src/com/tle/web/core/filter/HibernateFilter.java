package com.tle.web.core.filter;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.HibernateService;
import com.tle.core.user.CurrentInstitution;
import com.tle.web.dispatcher.AbstractWebFilter;
import com.tle.web.dispatcher.FilterResult;
import com.tle.web.dispatcher.WebFilterCallback;

@Bind
@Singleton
public class HibernateFilter extends AbstractWebFilter
{
	private Log LOGGER = LogFactory.getLog(HibernateFilter.class);

	@Inject
	private HibernateService hibernateService;

	@Override
	public FilterResult filterRequest(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		FilterResult result = new FilterResult();
		if( CurrentInstitution.get() != null )
		{
			final SessionFactory sessionFactory = hibernateService.getTransactionAwareSessionFactory("main", false); //$NON-NLS-1$

			if( !TransactionSynchronizationManager.hasResource(sessionFactory) )
			{
				LOGGER.debug("Opening single Hibernate Session in OpenSessionInViewFilter"); //$NON-NLS-1$
				Session session = SessionFactoryUtils.getSession(sessionFactory, true);
				TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
				result.setCallback(new WebFilterCallback()
				{
					@Override
					public void afterServlet(HttpServletRequest request, HttpServletResponse response)
					{
						// single session mode
						SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager
							.unbindResource(sessionFactory);
						LOGGER.debug("Closing single Hibernate Session in OpenSessionInViewFilter"); //$NON-NLS-1$
						SessionFactoryUtils.closeSession(sessionHolder.getSession());
					}
				});
			}
		}
		return result;
	}
}
