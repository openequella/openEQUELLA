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

package com.tle.beans.item;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemTaskId extends AbstractItemKey implements ItemKeyExtension
{
	private static final Pattern EXTRACT_TASK = Pattern.compile("^([^\\./]+)\\.([^/]+)/(\\d+)$"); //$NON-NLS-1$
	private static final long serialVersionUID = 1;

	private final String taskId;

	@SuppressWarnings("nls")
	public ItemTaskId(String fullid)
	{
		Matcher m = EXTRACT_TASK.matcher(fullid);
		if( m.matches() )
		{
			uuid = m.group(1);
			version = Integer.parseInt(m.group(3));
			taskId = m.group(2);
		}
		else
		{
			throw new IllegalArgumentException("String isn't an ItemTaskId:" + fullid);
		}
	}

	public ItemTaskId(String id, int version, String task)
	{
		super(id, version);
		this.taskId = task;
	}

	public ItemTaskId(ItemKey key, String taskId)
	{
		super(key.getUuid(), key.getVersion());
		this.taskId = taskId;
	}

	@Override
	public String toString(int version)
	{
		return getUuid() + '.' + taskId + '/' + version;
	}

	public String getTaskId()
	{
		return taskId;
	}

	@Override
	public String getExtensionId()
	{
		return "task"; //$NON-NLS-1$
	}

	@Override
	public boolean isDRMApplicable()
	{
		return false;
	}

	@Override
	public int hashCode()
	{
		return super.hashCode() + taskId.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if( !super.equals(obj) )
		{
			return false;
		}
		return taskId.equals(((ItemTaskId) obj).taskId);
	}

	public static ItemKey parse(String fullid)
	{
		Matcher m = EXTRACT_TASK.matcher(fullid);
		if( m.matches() )
		{
			return new ItemTaskId(m.group(1), Integer.parseInt(m.group(3)), m.group(2));
		}
		return new ItemId(fullid);
	}
}
