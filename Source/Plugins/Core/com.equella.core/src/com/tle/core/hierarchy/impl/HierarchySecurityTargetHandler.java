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
