package com.tle.web.core.listener;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.log4j.Logger;

import com.dytech.edge.web.WebConstants;
import com.tle.beans.Institution;
import com.tle.core.events.UserSessionLogoutEvent;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.institution.RunAsInstitution;
import com.tle.core.services.EventService;
import com.tle.core.services.user.UserSessionService;
import com.tle.core.user.CurrentInstitution;
import com.tle.core.user.UserState;

@Bind
@Singleton
public class UserSessionDestructionListener implements HttpSessionListener
{
	private static final Logger LOGGER = Logger.getLogger(UserSessionDestructionListener.class);

	@Inject
	private UserSessionService sessionService;
	@Inject
	private InstitutionService institutionService;
	@Inject
	private EventService eventService;
	@Inject
	private RunAsInstitution runAs;

	@Override
	public void sessionCreated(HttpSessionEvent event)
	{
		// We don't care about these
		if( LOGGER.isDebugEnabled() )
		{
			final String sessionId = event.getSession().getId();
			LOGGER.debug(sessionId + " session created");
		}
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent event)
	{
		final HttpSession session = event.getSession();
		final String sessionId = session.getId();
		if( LOGGER.isDebugEnabled() )
		{
			LOGGER.debug(sessionId + " session destroyed");
		}

		try
		{
			for( final Institution institution : institutionService.getAvailableMap().values() )
			{
				runAs.executeAsSystem(institution, new Runnable()
				{

					@Override
					public void run()
					{
						UserState userState = sessionService.getAttributeFromSession(session, institution,
							WebConstants.KEY_USERSTATE);
						if( userState != null )
						{
							if( LOGGER.isDebugEnabled() )
							{
								LOGGER.debug(sessionId + " firing logout event");
							}
							eventService.publishApplicationEvent(new UserSessionLogoutEvent(userState, true));
						}
					}
				});
			}
		}
		finally
		{
			CurrentInstitution.remove();
		}
	}
}
