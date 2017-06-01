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

package com.tle.web.institution.database;

import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.renderers.SpanRenderer;

public class MigrationRow
{
	private final int percent;
	private final String id;
	private final SpanRenderer label;
	private final HtmlComponentState errorLink;

	public MigrationRow(int percent, String id, SpanRenderer label, HtmlComponentState errorLink)
	{
		this.percent = percent;
		this.id = id;
		this.label = label;
		this.errorLink = errorLink;
	}

	public int getPercent()
	{
		return percent;
	}

	public String getId()
	{
		return id;
	}

	public SpanRenderer getLabel()
	{
		return label;
	}

	public HtmlComponentState getErrorLink()
	{
		return errorLink;
	}
}