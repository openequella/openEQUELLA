package com.tle.core.user;

import java.io.Serializable;
import java.util.Map;

import com.tle.common.Check;

/**
 * @author Nicholas Read
 */
public class SessionMap implements Serializable
{
	// SESSION KEYS //////////////////////////////////////////////////////////

	private static final long serialVersionUID = 1L;
	public static final String SKEY_LMS_OBJECT = "lms-object"; //$NON-NLS-1$
	public static final String SKEY_NAVIGATION_DATA = "navigation-data"; //$NON-NLS-1$
	public static final String SKEY_CAL_SESSION = "cal-session"; //$NON-NLS-1$
	public static final String SKEY_DOWNLOADS_QUEUE = "downloads-queue"; //$NON-NLS-1$
	public static final String SKEY_WIZARDS = "wizards"; //$NON-NLS-1$
	public static final String SKEY_ITEM_PREVIEWS = "item-previews"; //$NON-NLS-1$
	public static final String SKEY_VIEW_ITEM_STATES = "view-item-states"; //$NON-NLS-1$
	public static final String SKEY_SCORM_API = "scorm-api"; //$NON-NLS-1$
	public static final String SKEY_LOCALE = "locale"; //$NON-NLS-1$
	public static final String SKEY_SUMMARY_VIEWED_ITEMS = "summary-viewed-items"; //$NON-NLS-1$
	public static final String SKEY_CONTENT_VIEWED_ITEMS = "content-viewed-items"; //$NON-NLS-1$
	public static final String USER_STATE_KEY = "user-state"; //$NON-NLS-1$

	private final String sessionId;
	private final Map<String, Object> state;

	private boolean changed;

	public SessionMap(String sessionId, Map<String, Object> state)
	{
		Check.checkNotNull(sessionId);
		Check.checkNotNull(state);

		this.sessionId = sessionId;
		this.state = state;

		changed = false;
	}

	public Object get(String key)
	{
		return state.get(key);
	}

	public void set(String key, Object value)
	{
		state.put(key, value);
		changed = true;
	}

	public void remove(String key)
	{
		state.remove(key);
		changed = true;
	}

	public String getSessionId()
	{
		return sessionId;
	}

	public UserState getUserState()
	{
		return (UserState) get(USER_STATE_KEY);
	}

	public void setUserState(UserState userState)
	{
		if( getUserState() != userState )
		{
			set(USER_STATE_KEY, userState);
		}
	}

	public boolean hasChanged()
	{
		return changed;
	}

	public Map<String, Object> getState()
	{
		return state;
	}
}
