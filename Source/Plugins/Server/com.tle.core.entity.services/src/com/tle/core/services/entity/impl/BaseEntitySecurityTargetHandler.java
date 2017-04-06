package com.tle.core.services.entity.impl;

import static com.tle.common.security.SecurityConstants.TARGET_BASEENTITY;

import java.util.Objects;
import java.util.Set;

import javax.inject.Singleton;

import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.common.EntityPack;
import com.tle.core.guice.Bind;
import com.tle.core.security.SecurityTargetHandler;

@Bind
@Singleton
public class BaseEntitySecurityTargetHandler implements SecurityTargetHandler
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
		final long id = target instanceof BaseEntity ? ((BaseEntity) target).getId() : ((BaseEntityLabel) target)
			.getId();
		return TARGET_BASEENTITY + ":" + id;
	}

	@Override
	public Object transform(Object target)
	{
		return ((EntityPack<?>) target).getEntity();
	}

	@Override
	public boolean isOwner(Object target, String userId)
	{
		final String owner = target instanceof BaseEntity ? ((BaseEntity) target).getOwner()
			: ((BaseEntityLabel) target).getOwner();
		return Objects.equals(owner, userId);
	}
}
