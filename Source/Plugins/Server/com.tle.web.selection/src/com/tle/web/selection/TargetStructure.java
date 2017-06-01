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

package com.tle.web.selection;

import java.util.Collections;
import java.util.Map;

import com.google.common.collect.Maps;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;

/**
 * @author Aaron
 */
@NonNullByDefault
public class TargetStructure extends TargetFolder
{
	private Map<String, String> attributes = Maps.newHashMap();
	private boolean noTargets;

	public Map<String, String> getAttributes()
	{
		return Collections.unmodifiableMap(attributes);
	}

	public void putAttribute(String key, String value)
	{
		attributes.put(key, value);
	}

	@Nullable
	public String getAttribute(String key)
	{
		return attributes.get(key);
	}

	public boolean isNoTargets()
	{
		return noTargets;
	}

	public void setNoTargets(boolean noTargets)
	{
		this.noTargets = noTargets;
	}
}
