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

package com.tle.common.wizard.controls.universal;

import java.util.Set;

import com.dytech.edge.wizard.beans.control.CustomControl;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public class UniversalSettings
{
	private static final String KEY_ALLOW_MULTIPLE = "AllowMultiple";
	private static final String KEY_ALLOW_PREVIEWS = "AllowPreviews";
	private static final String KEY_ATTACHMENT_TYPES = "AttachmentTypes";
	private static final String KEY_ENABLE_MAX_FILES = "EnableMaxFiles";
	private static final String MAX_FILES = "MaxFiles";

	protected final CustomControl wrapped;

	public UniversalSettings(CustomControl wrapped)
	{
		this.wrapped = wrapped;
		this.wrapped.setClassType("universal");
	}

	public CustomControl getWrapped()
	{
		return wrapped;
	}

	public boolean isMultipleSelection()
	{
		return wrapped.getBooleanAttribute(KEY_ALLOW_MULTIPLE);
	}

	public void setMultipleSelection(boolean allowMultiple)
	{
		wrapped.getAttributes().put(KEY_ALLOW_MULTIPLE, allowMultiple);
	}

	public boolean isAllowPreviews()
	{
		return wrapped.getBooleanAttribute(KEY_ALLOW_PREVIEWS);
	}

	public void setAllowPreviews(boolean allowPreviews)
	{
		wrapped.getAttributes().put(KEY_ALLOW_PREVIEWS, allowPreviews);
	}

	public Set<String> getAttachmentTypes()
	{
		return wrapped.ensureSetAttribute(KEY_ATTACHMENT_TYPES);
	}

	public void setAttachmentTypes(Set<String> attachmentTypes)
	{
		wrapped.getAttributes().put(KEY_ATTACHMENT_TYPES, attachmentTypes);
	}

	public boolean isMaxFilesEnabled()
	{
		return wrapped.getBooleanAttribute(KEY_ENABLE_MAX_FILES);
	}

	public void setMaxFilesEnabled(boolean enableMaxFiles)
	{
		wrapped.getAttributes().put(KEY_ENABLE_MAX_FILES, enableMaxFiles);
	}

	public int getMaxFiles()
	{
		Integer maxFiles = (Integer) wrapped.getAttributes().get(MAX_FILES);
		if( maxFiles != null )
		{
			return maxFiles;
		}

		return 0;

	}

	public void setMaxFiles(int maxFiles)
	{
		wrapped.getAttributes().put(MAX_FILES, maxFiles);
	}

}
