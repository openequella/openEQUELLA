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

package com.tle.common.workflow;

public class TaskModerator
{
	public enum Type
	{
		USER, GROUP, ROLE
	}

	private final Type type;
	private final String id;
	private final boolean accepted;

	public TaskModerator(String id, Type type, boolean accepted)
	{
		this.id = id;
		this.type = type;
		this.accepted = accepted;
	}

	public Type getType()
	{
		return type;
	}

	public String getId()
	{
		return id;
	}

	public boolean isAccepted()
	{
		return accepted;
	}
}
