package com.tle.core.settings.security;

import java.util.Set;

import javax.inject.Singleton;

import com.tle.common.security.SettingsTarget;
import com.tle.core.guice.Bind;
import com.tle.core.security.SecurityTargetHandler;

@Bind
@Singleton
public class SettingsSecurityTargetHandler implements SecurityTargetHandler
{
	@Override
	public void gatherAllLabels(Set<String> labels, Object target)
	{
		labels.add(getPrimaryLabel(target));
	}

	@Override
	@SuppressWarnings("nls")
	public String getPrimaryLabel(Object target)
	{
		return "C:" + ((SettingsTarget) target).getId();
	}

	@Override
	public Object transform(Object target)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isOwner(Object target, String userId)
	{
		throw new UnsupportedOperationException();
	}
}
