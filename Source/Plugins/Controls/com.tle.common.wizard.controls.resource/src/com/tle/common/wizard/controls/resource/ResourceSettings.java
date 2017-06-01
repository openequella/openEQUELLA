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

package com.tle.common.wizard.controls.resource;

import java.util.Set;

import com.dytech.edge.wizard.beans.control.CustomControl;
import com.tle.common.wizard.controls.universal.UniversalSettings;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public class ResourceSettings extends UniversalSettings
{
	private static final String KEY_ALLOW_SELECTION = "AllowSelection";
	private static final String KEY_RELATION_TYPE = "RelationType";
	private static final String KEY_RESTRICT_SWITCH_PREFIX = "IsRestricted";
	private static final String KEY_RESTRICT_CHOICES_PREFIX = "RestrictTo";

	public static final String KEY_RESTRICT_COLLECTIONS = "Collections";
	public static final String KEY_RESTRICT_POWERSEARCHES = "PowerSearches";
	public static final String KEY_RESTRICT_CONTRIBUTION = "Contribution";
	public static final String KEY_RESTRICT_DYNACOLLECTION = "DynaCollection";

	public ResourceSettings(CustomControl wrapped)
	{
		super(wrapped);
	}

	public ResourceSettings(UniversalSettings settings)
	{
		super(settings.getWrapped());
	}

	public AllowedSelection getAllowedSelection()
	{
		return wrapped.get(KEY_ALLOW_SELECTION, AllowedSelection.class, AllowedSelection.ANYTHING);
	}

	public void setAllowedSelection(AllowedSelection as)
	{
		wrapped.getAttributes().put(KEY_ALLOW_SELECTION, as.toString());
	}

	public String getRelationType()
	{
		return (String) wrapped.getAttributes().get(KEY_RELATION_TYPE);
	}

	public void setRelationType(String relationType)
	{
		wrapped.getAttributes().put(KEY_RELATION_TYPE, relationType);
	}

	public boolean isRestricted(String key)
	{
		return wrapped.getBooleanAttribute(KEY_RESTRICT_SWITCH_PREFIX + key);
	}

	public void setRestricted(String key, boolean b)
	{
		wrapped.getAttributes().put(KEY_RESTRICT_SWITCH_PREFIX + key, b);
	}

	public Set<String> getRestrictedTo(String key)
	{
		return wrapped.ensureSetAttribute(KEY_RESTRICT_CHOICES_PREFIX + key);
	}

	public void setRestrictedTo(String key, Set<String> value)
	{
		wrapped.getAttributes().put(KEY_RESTRICT_CHOICES_PREFIX + key, value);
	}

	public static enum AllowedSelection
	{
		ITEMS(true, false, false), ATTACHMENTS(false, true, false), PACKAGES(false, false, true),
		ITEMS_AND_ATTACHMENTS(true, true, false), ANYTHING(true, true, true);

		private final boolean items;
		private final boolean attachments;
		private final boolean packages;

		private AllowedSelection(boolean items, boolean attachments, boolean packages)
		{
			this.items = items;
			this.attachments = attachments;
			this.packages = packages;
		}

		public boolean isItems()
		{
			return items;
		}

		public boolean isAttachments()
		{
			return attachments;
		}

		public boolean isPackages()
		{
			return packages;
		}
	}
}
