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

package com.tle.core.settings.security;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.SettingsTarget;
import com.tle.common.security.remoting.RemotePrivilegeTreeService.SecurityTarget;
import com.tle.common.security.remoting.RemotePrivilegeTreeService.TargetId;
import com.tle.core.security.PrivilegeTreeProvider;
import com.tle.core.security.TLEAclManager;
import com.tle.exceptions.AccessDeniedException;

@SuppressWarnings("nls")
public abstract class AbstractSettingsPrivilegeTreeProvider implements PrivilegeTreeProvider
{
	public enum Type
	{
		SYSTEM_SETTING(Node.ALL_SYSTEM_SETTINGS, Node.SYSTEM_SETTING, "EDIT_SYSTEM_SETTINGS"), MANAGEMENT_PAGE(
			Node.ALL_MANAGING, Node.MANAGING, "VIEW_MANAGEMENT_PAGE");

		private final Node allNode;
		private final Node singularNode;
		private final Collection<String> privileges;

		private Type(Node allNode, Node singularNode, String privilege)
		{
			this.allNode = allNode;
			this.singularNode = singularNode;
			this.privileges = Arrays.asList(privilege);
		}

		public Node getAllNode()
		{
			return allNode;
		}

		public Node getSingularNode()
		{
			return singularNode;
		}

		public Collection<String> getPrivileges()
		{
			return privileges;
		}
	}

	@Inject
	private TLEAclManager aclService;
	@Inject
	private SettingsSecurityTargetHandler handler;

	private final Type type;
	private final String nameKey;
	private final SettingsTarget settingTarget;

	private transient String targetLabel;

	protected AbstractSettingsPrivilegeTreeProvider(Type type, String nameKey, SettingsTarget settingTarget)
	{
		this.type = type;
		this.nameKey = nameKey;
		this.settingTarget = settingTarget;
	}

	@Override
	public void mapTargetIdsToNames(Collection<TargetId> targetIds, Map<TargetId, String> results)
	{
		if( targetLabel == null )
		{
			targetLabel = handler.getPrimaryLabel(settingTarget);
		}

		for( TargetId targetId : targetIds )
		{
			if( targetId.getTarget().equals(targetLabel) )
			{
				results.put(targetId, CurrentLocale.get(nameKey));
			}
		}
	}

	@Override
	public void gatherChildTargets(List<SecurityTarget> childTargets, SecurityTarget target)
	{
		if( target != null && target.getTargetType() == type.getAllNode() )
		{
			childTargets.add(new SecurityTarget(CurrentLocale.get(nameKey), type.getSingularNode(), settingTarget,
				false));
		}
	}

	public void checkAuthorised()
	{
		if( !isAuthorised() )
		{
			throw new AccessDeniedException(CurrentLocale.get("com.tle.core.search.error.noaccess"));
		}
	}

	public boolean isAuthorised()
	{
		return !aclService.filterNonGrantedPrivileges(settingTarget, type.getPrivileges()).isEmpty();
	}
}
