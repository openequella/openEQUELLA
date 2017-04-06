package com.tle.core.hierarchy.impl;

import static com.tle.common.security.SecurityConstants.TARGET_HIERARCHY_TOPIC;

import java.util.Set;

import javax.inject.Singleton;

import com.tle.beans.hierarchy.HierarchyPack;
import com.tle.beans.hierarchy.HierarchyTopic;
import com.tle.beans.hierarchy.HierarchyTreeNode;
import com.tle.core.guice.Bind;
import com.tle.core.security.SecurityTargetHandler;

@Bind
@Singleton
@SuppressWarnings("nls")
public class HierarchySecurityTargetHandler implements SecurityTargetHandler
{
	@Override
	public void gatherAllLabels(Set<String> labels, Object target)
	{
		labels.add(getPrimaryLabel(target));
	}

	@Override
	public String getPrimaryLabel(Object target)
	{
		final long topicId = target instanceof HierarchyTopic ? ((HierarchyTopic) target).getId()
			: ((HierarchyTreeNode) target).getId();
		return TARGET_HIERARCHY_TOPIC + ":" + topicId;
	}

	@Override
	public Object transform(Object target)
	{
		return ((HierarchyPack) target).getTopic();
	}

	@Override
	public boolean isOwner(Object target, String userId)
	{
		throw new UnsupportedOperationException();
	}
}
