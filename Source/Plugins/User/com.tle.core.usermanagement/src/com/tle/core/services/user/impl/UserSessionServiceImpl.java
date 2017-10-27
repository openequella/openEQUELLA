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

package com.tle.core.services.user.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.dytech.edge.web.WebConstants;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.Institution;
import com.tle.common.Pair;
import com.tle.core.events.UserSessionLoginEvent;
import com.tle.core.events.UserSessionLogoutEvent;
import com.tle.core.events.listeners.UserSessionLoginListener;
import com.tle.core.events.listeners.UserSessionLogoutListener;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.plugins.SerialisedValue;
import com.tle.core.replicatedcache.BatchedCache;
import com.tle.core.replicatedcache.ReplicatedCacheService;
import com.tle.core.replicatedcache.ReplicatedCacheService.ReplicatedCache;
import com.tle.core.services.user.UserSessionService;
import com.tle.core.services.user.UserSessionTimestamp;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.UserState;

/**
 * @author Nicholas Read
 */
@NonNullByDefault
@Bind(UserSessionService.class)
@Singleton
@SuppressWarnings("nls")
public final class UserSessionServiceImpl
	implements
		UserSessionService,
		UserSessionLoginListener,
		UserSessionLogoutListener
{
	private static final Logger LOGGER = Logger.getLogger(UserSessionService.class);

	private final ThreadLocal<SessionState> sessionLocal = new ThreadLocal<SessionState>();

	private BatchedCache<UserSessionTimestamp> allSessions;

	@Inject
	public void setServices(ReplicatedCacheService rcs, InstitutionService is)
	{
		ReplicatedCache<UserSessionTimestamp> cache = rcs.getCache("USER_SESSIONS", 1000, 2, TimeUnit.HOURS);

		// User session timestamps are updated rapidly, and in reality they're
		// not essential functionality. Use the batcher so that we only update
		// the replicated cache each minute or two.
		allSessions = new BatchedCache<UserSessionTimestamp>(is, cache);
		allSessions.setAutoFlush(2, TimeUnit.MINUTES);
	}

	@Nullable
	@Override
	public <T> T getAttribute(String key)
	{
		HttpSession session = getCurrentSession(false);
		if( session == null )
		{
			return null;
		}
		return getAttributeFromSession(session, CurrentInstitution.get(), key);
	}

	@Override
	public void removeAttribute(String key)
	{
		HttpSession session = getCurrentSession(false);
		if( session != null )
		{
			session.removeAttribute(getKey(key));
		}
	}

	@Override
	public void setAttribute(String key, Object attribute)
	{
		if( attribute == null )
		{
			LOGGER.warn("Please use 'removeAttribute(key)' intead of 'setAttribute(key, null)'");
			removeAttribute(key);
			return;
		}

		HttpSession session = getCurrentSession(true);
		synchronized( session )
		{
			setAttributeInternal(session, key, attribute);
		}
	}

	private void setAttributeInternal(HttpSession session, String key, Object attribute)
	{
		final String realKey = getKey(key);
		SerialisedValue<?> oldValue = (SerialisedValue<?>) session.getAttribute(realKey);
		SerialisedValue<?> newValue = new SerialisedValue<Object>(attribute);
		if( oldValue == null || !Arrays.equals(oldValue.getData(), newValue.getData()) )
		{
			session.setAttribute(realKey, newValue);
		}
	}

	@Override
	public void bindRequest(HttpServletRequest request)
	{
		sessionLocal.set(new SessionState(request));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAttributeFromSession(HttpSession session, Institution institution, String attribute)
	{
		synchronized( session )
		{
			SerialisedValue<T> sessionValue = (SerialisedValue<T>) session.getAttribute(getKey(attribute));
			if( sessionValue == null )
			{
				return null;
			}
			return sessionValue.getObject();
		}
	}

	@Override
	public boolean isSessionPrevented()
	{
		return sessionLocal.get().isSessionPrevented();
	}

	@Override
	public void preventSessionUse()
	{
		SessionState sessionState = sessionLocal.get();
		sessionState.setSession(null);
		sessionState.setSessionPrevented(true);
	}

	@Override
	public HttpServletRequest getAssociatedRequest()
	{
		SessionState sessionState = sessionLocal.get();
		if( sessionState != null )
		{
			return sessionState.getRequest();
		}
		return null;
	}

	@Override
	public boolean isSessionAvailable()
	{
		SessionState sessionState = sessionLocal.get();
		return sessionState != null && sessionState.getSession() != null;
	}

	@Override
	public void unbind()
	{
		sessionLocal.remove();
	}

	@Nullable
	private HttpSession getCurrentSession(boolean force)
	{
		SessionState state = sessionLocal.get();
		if( state == null )
		{
			throw new RuntimeException("Thread has no request bound to it");
		}
		if( force && state.sessionPrevented )
		{
			throw new RuntimeException("Trying to get session when they are disallowed");
		}
		HttpSession session = state.getSession();
		if( session == null )
		{
			session = state.getRequest().getSession(force);
			if( session != null )
			{
				doEnsure(session);
			}
			state.setSession(session);
		}
		return session;
	}

	@Override
	public void forceSession()
	{
		HttpSession session = getCurrentSession(true);
		doEnsure(session);
	}

	private void doEnsure(HttpSession session)
	{
		synchronized( session )
		{
			UserState userState = CurrentUser.getUserState();
			if( userState != null && userState.isNeedsSessionUpdate() )
			{
				userState.updatedInSession();
				setAttributeInternal(session, WebConstants.KEY_USERSTATE, userState);
			}
		}
	}

	private StringBuilder getKeyPrefix()
	{
		StringBuilder sb = new StringBuilder("i-");
		Institution inst = CurrentInstitution.get();
		sb.append(inst == null ? '$' : Long.toString(inst.getUniqueId()));
		return sb.append('-');
	}

	private String getKey(String key)
	{
		return getKeyPrefix().append(key).toString();
	}

	@Override
	public String createUniqueKey()
	{
		HttpSession session = getCurrentSession(true);
		synchronized( session )
		{
			Long idupto = (Long) getAttribute("IDS");
			if( idupto == null )
			{
				idupto = 0L;
			}
			idupto++;
			setAttribute("IDS", idupto);
			return Long.toString(idupto);
		}
	}

	@Override
	public void nudgeSession()
	{
		UserState userState = CurrentUser.getUserState();
		if( userState != null )
		{
			HttpSession session = getCurrentSession(false);
			if( session != null )
			{
				synchronized( session )
				{
					long twoMinutesAgo = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(2);
					long lastAccessedTime = session.getLastAccessedTime();
					if( lastAccessedTime > twoMinutesAgo )
					{
						nudgeSession(userState.getSessionID());
					}
				}
			}
		}
	}

	private void nudgeSession(String sessionId)
	{
		if( CurrentInstitution.get() == null )
		{
			return;
		}

		if( LOGGER.isDebugEnabled() )
		{
			LOGGER.debug("nudgeSession (enter) " + sessionId);
		}
		synchronized( allSessions )
		{
			Optional<UserSessionTimestamp> stamp = allSessions.get(sessionId);
			if( stamp.isPresent() )
			{
				allSessions.put(sessionId, stamp.get().updatedAccessed(new Date()));
			}
			else if( LOGGER.isDebugEnabled() )
			{
				LOGGER.debug("nudgeSession (there was no session to nudge) " + sessionId);
			}
		}
	}

	@Override
	public void userSessionDestroyedEvent(UserSessionLogoutEvent event)
	{
		if( event.getUserState().isAuditable() )
		{
			if( LOGGER.isDebugEnabled() )
			{
				LOGGER.debug("userSessionDestroyedEvent (enter) " + event.getSessionId());
			}

			allSessions.invalidate(event.getSessionId());
		}

		if( event.isEntireHttpSessionDestroyed() )
		{
			// No use deleting all the bits and pieces from the session if the
			// HTTP session is being killed
			return;
		}

		clearSession();
	}

	private void clearSession()
	{
		final HttpSession session = getCurrentSession(false);
		if( session != null )
		{
			synchronized( session )
			{
				final String keyPrefix = getKeyPrefix().toString();

				for( Enumeration<?> e = session.getAttributeNames(); e.hasMoreElements(); )
				{
					String name = (String) e.nextElement();
					if( name.startsWith(keyPrefix) )
					{
						session.removeAttribute(name);
					}
				}
			}
		}
	}

	@Override
	public void userSessionCreatedEvent(UserSessionLoginEvent event)
	{
		UserState userState = event.getUserState();
		if( !userState.isAuditable() )
		{
			return;
		}
		final String sessionId = userState.getSessionID();
		synchronized( allSessions )
		{
			Optional<UserSessionTimestamp> ostamp = allSessions.get(sessionId);
			if( !ostamp.isPresent() )
			{
				if( LOGGER.isDebugEnabled() )
				{
					LOGGER.debug("userLoggedIn (new session created) " + sessionId);
				}
				UserSessionTimestamp stamp = new UserSessionTimestamp(sessionId, userState.isGuest(), userState
					.getUserBean().getUsername(), userState.getHostAddress());
				allSessions.putAndReplicateNow(sessionId, stamp);
			}
			else
			{
				if( LOGGER.isDebugEnabled() )
				{
					LOGGER.debug("userLoggedIn (login over login) " + sessionId);
				}
				nudgeSession(sessionId);
			}
		}
	}

	@Override
	public Iterable<UserSessionTimestamp> getInstitutionSessions()
	{
		final Iterable<Pair<String, UserSessionTimestamp>> usts = allSessions.iterate("");
		return Iterables.transform(usts, new Function<Pair<String, UserSessionTimestamp>, UserSessionTimestamp>()
		{
			@Override
			@Nullable
			public UserSessionTimestamp apply(@Nullable Pair<String, UserSessionTimestamp> input)
			{
				return input.getSecond();
			}
		});
	}

	@Override
	public Object getSessionLock()
	{
		final HttpSession session = getCurrentSession(false);
		if( session == null )
		{
			return new Object();
		}
		return session;
	}

	private static class SessionState
	{
		private final HttpServletRequest request;
		private HttpSession session;
		private boolean sessionPrevented;

		public SessionState(HttpServletRequest request)
		{
			this.request = request;
		}

		public boolean isSessionPrevented()
		{
			return sessionPrevented;
		}

		public void setSessionPrevented(boolean sessionPrevented)
		{
			this.sessionPrevented = sessionPrevented;
		}

		public HttpServletRequest getRequest()
		{
			return request;
		}

		public HttpSession getSession()
		{
			return session;
		}

		public void setSession(HttpSession session)
		{
			this.session = session;
		}
	}
}
