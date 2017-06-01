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

package com.tle.beans.search;

import com.tle.beans.entity.FederatedSearch;

/**
 * Settings for search types that return results in XML format
 * 
 * @author Aaron
 */
@SuppressWarnings("nls")
public abstract class XmlBasedSearchSettings extends SearchSettings
{
	private static final String SCHEMA_INPUT_TRANSFORM = "schemaInputTransform";
	private static final String DISPLAY_XSLT = "xslt";

	private String schemaInputTransform;
	private String displayXslt;

	public XmlBasedSearchSettings()
	{
	}

	public XmlBasedSearchSettings(FederatedSearch search)
	{
		this();
		load(search);
	}

	@Override
	protected void _load()
	{
		schemaInputTransform = get(SCHEMA_INPUT_TRANSFORM, schemaInputTransform);
		displayXslt = get(DISPLAY_XSLT, displayXslt);
	}

	@Override
	protected void _save()
	{
		put(SCHEMA_INPUT_TRANSFORM, schemaInputTransform);
		put(DISPLAY_XSLT, displayXslt);
	}

	public String getSchemaInputTransform()
	{
		return schemaInputTransform;
	}

	public void setSchemaInputTransform(String schemaInputTransform)
	{
		this.schemaInputTransform = schemaInputTransform;
	}

	public String getDisplayXslt()
	{
		return displayXslt;
	}

	public void setDisplayXslt(String displayXslt)
	{
		this.displayXslt = displayXslt;
	}
}
