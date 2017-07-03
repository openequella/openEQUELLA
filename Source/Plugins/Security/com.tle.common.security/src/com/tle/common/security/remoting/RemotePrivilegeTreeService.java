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

package com.tle.common.security.remoting;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.tle.common.Check;
import com.tle.common.security.PrivilegeTree.Node;

public interface RemotePrivilegeTreeService
{
	/**
	 * Returns a map of pretty names for security targets IDs. For example,
	 * "B:1234" becomes "John's Collection".
	 */
	Map<TargetId, String> mapTargetIdsToNames(Collection<TargetId> targetIds);

	/**
	 * Returns a list of child targets for the given target identifier.
	 * 
	 * @param target the target node to return the children for.
	 *            <code>null</code> indicates root targets.
	 * @return list of SecurityTarget children. An empty list is returned if
	 *         there are no children.
	 */
	List<SecurityTarget> getChildTargets(SecurityTarget target);

	class TargetId implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private final int priority;
		private final String target;

		public TargetId(int priority, String target)
		{
			this.priority = Math.abs(priority);
			this.target = target;
		}

		public int getPriority()
		{
			return priority;
		}

		public String getTarget()
		{
			return target;
		}

		@Override
		@SuppressWarnings("nls")
		public String toString()
		{
			return "TargetId[" + priority + "," + target + "]";
		}

		@Override
		public int hashCode()
		{
			return Check.getHashCode(priority, target);
		}

		@Override
		public boolean equals(Object obj)
		{
			if( this == obj )
			{
				return true;
			}
			else if( obj == null || !TargetId.class.isAssignableFrom(obj.getClass()) )
			{
				return false;
			}
			else
			{
				TargetId rhs = (TargetId) obj;
				return priority == rhs.priority && target.equals(rhs.target);
			}
		}
	}

	class SecurityTarget implements Serializable
	{
		private static final long serialVersionUID = 1L;

		/**
		 * This contains real text rather an I18N key because the key could be
		 * dereferenced by the resource centre or the admin console which are
		 * different bundle groups. When the security tree editor is moved from
		 * the AC and into the RC, then this can change to being a key.
		 */
		private final String displayName;
		private final Node targetType;
		private final Serializable target;
		private final boolean childTargets;

		public SecurityTarget(String displayName, Node targetType, Serializable target, boolean childTargets)
		{
			this.displayName = displayName;
			this.targetType = targetType;
			this.target = target;
			this.childTargets = childTargets;
		}

		public String getDisplayName()
		{
			return displayName;
		}

		public Node getTargetType()
		{
			return targetType;
		}

		public Serializable getTarget()
		{
			return target;
		}

		public boolean hasChildTargets()
		{
			return childTargets;
		}
	}
}
