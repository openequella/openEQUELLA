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

package com.tle.common.searching;

import com.tle.common.Pair;

public class Field extends Pair<String, String>
{
	private static final long serialVersionUID = 1L;

	public Field(String field, String value)
	{
		super(field, value);
	}

	public String getField()
	{
		return getFirst();
	}

	public String getValue()
	{
		return getSecond();
	}

	@Override
	public String toString()
	{
		return getField() + ':' + getValue();
	}
}
