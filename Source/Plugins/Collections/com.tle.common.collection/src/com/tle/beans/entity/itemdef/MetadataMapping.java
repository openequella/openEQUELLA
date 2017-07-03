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

package com.tle.beans.entity.itemdef;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import com.tle.beans.entity.itemdef.mapping.HTMLMapping;
import com.tle.beans.entity.itemdef.mapping.IMSMapping;
import com.tle.beans.entity.itemdef.mapping.LiteralMapping;

public class MetadataMapping implements Serializable
{
	private static final long serialVersionUID = 1;

	private Collection<IMSMapping> imsMapping = new ArrayList<IMSMapping>();
	private Collection<HTMLMapping> htmlMapping = new ArrayList<HTMLMapping>();
	private Collection<LiteralMapping> literalMapping = new ArrayList<LiteralMapping>();

	public Collection<HTMLMapping> getHtmlMapping()
	{
		return htmlMapping;
	}

	public Collection<IMSMapping> getImsMapping()
	{
		return imsMapping;
	}

	public Collection<LiteralMapping> getLiteralMapping()
	{
		return literalMapping;
	}
}
