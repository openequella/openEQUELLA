package com.tle.core.activation.service;

import java.util.Objects;
import java.util.Set;

import javax.inject.Singleton;

import com.tle.beans.activation.ActivateRequest;
import com.tle.core.guice.Bind;
import com.tle.core.security.SecurityTargetHandler;

@Bind
@Singleton
public class ActivateRequestSecurityTargetHandler implements SecurityTargetHandler
{
	@Override
	public void gatherAllLabels(Set<String> labels, Object target)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getPrimaryLabel(Object target)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isOwner(Object target, String userId)
	{
		return Objects.equals(((ActivateRequest) target).getUser(), userId);
	}

	@Override
	public Object transform(Object target)
	{
		return ((ActivateRequest) target).getItem();
	}
}
