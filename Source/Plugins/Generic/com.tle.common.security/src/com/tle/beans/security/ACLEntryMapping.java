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

package com.tle.beans.security;

import java.io.Serializable;

public class ACLEntryMapping implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final long id;
	private final char grant;
	private final int priority;
	private final String target;
	private final String expression;

	public ACLEntryMapping(long id, char grant, int priority, String target, String expression)
	{
		this.id = id;
		this.grant = grant;
		this.priority = priority;
		this.target = target;
		this.expression = expression;
	}

	public int getPriority()
	{
		return priority;
	}

	public String getTarget()
	{
		return target;
	}

	public String getExpression()
	{
		return expression;
	}

	public char getGrant()
	{
		return grant;
	}

	public long getId()
	{
		return id;
	}
}
