package com.tle.web.template.section;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import com.tle.common.ExpiringValue;
import com.tle.core.services.user.UserSessionService;
import com.tle.web.sections.SectionInfo;

public abstract class AbstractCachedMenuContributor<T> implements MenuContributor
{
	private static final long ONE_MINUTE = TimeUnit.MINUTES.toMillis(1);
	@Inject
	private UserSessionService userSessionService;

	@Override
	public void clearCachedData()
	{
		userSessionService.removeAttribute(getSessionKey());
	}

	@Override
	public List<MenuContribution> getMenuContributions(SectionInfo info)
	{
		if( currentUserCanView(info) )
		{
			ExpiringValue<T> cachedCount = userSessionService.getAttribute(getSessionKey());
			if( cachedCount == null || cachedCount.isTimedOut() )
			{
				cachedCount = ExpiringValue.expireAfter(getCachedObject(info), cacheMillis(), TimeUnit.MILLISECONDS);
				userSessionService.setAttribute(getSessionKey(), cachedCount);
			}
			MenuContribution contribution = getContribution(info, cachedCount.getValue());
			if( contribution != null )
			{
				return Collections.singletonList(contribution);
			}
		}
		return Collections.emptyList();
	}

	protected abstract MenuContribution getContribution(SectionInfo info, T value);

	protected abstract T getCachedObject(SectionInfo info);

	protected long cacheMillis()
	{
		return ONE_MINUTE;
	}

	protected boolean currentUserCanView(SectionInfo info)
	{
		return true;
	}

	protected abstract String getSessionKey();

}
