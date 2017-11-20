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

package com.tle.common.interfaces;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

public class BaseEntityReference
{
	private final Map<String, Object> extras = new HashMap<String, Object>();

	private String uuid;
	private I18NString name;

	public BaseEntityReference()
	{
		// nothing
	}

	public BaseEntityReference(String uuid)
	{
		this.uuid = uuid;
	}

	public BaseEntityReference(String uuid, I18NString name)
	{
		this.uuid = uuid;
		this.name = name;
	}

	@JsonAnyGetter
	public Map<String, Object> any()
	{
		return extras;
	}

	@JsonAnySetter
	public void set(String key, Object value)
	{
		extras.put(key, value);
	}

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public I18NString getName()
	{
		return name;
	}

	public void setName(I18NString name)
	{
		this.name = name;
	}

}
