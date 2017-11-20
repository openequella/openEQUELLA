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

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * We might want to consider some sort of referencing standard, e.g.
 * http://dojotoolkit.org/reference-guide/dojox/json/ref.html
 * 
 * @author Aaron
 */
@XmlRootElement
public class UuidReference
{
	private String uuid;

	public UuidReference()
	{
	}

	public UuidReference(String uuid)
	{
		this.uuid = uuid;
	}

	@JsonProperty("$ref")
	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}
}
