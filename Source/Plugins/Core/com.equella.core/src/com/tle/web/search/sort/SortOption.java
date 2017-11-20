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

package com.tle.web.search.sort;

import com.tle.common.searching.Search.SortType;
import com.tle.common.searching.SortField;
import com.tle.common.searching.SortField.Type;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.KeyLabel;

public class SortOption
{
	static
	{
		PluginResourceHandler.init(SortOption.class);
	}

	@PlugKey("sortsection.")
	private static String KEY_OPTION;

	private final Label label; // The i18n key
	private final String value; // The value for the drop box
	protected final SortField field; // The field the lucene index sorts on

	public SortOption(SortType type)
	{
		this.value = type.name().toLowerCase();
		this.label = new KeyLabel(KEY_OPTION + this.value);
		this.field = type.getSortField();
	}

	public SortOption(Label label, String value, String field, boolean reverse)
	{
		super();
		this.label = label;
		this.value = value;
		this.field = new SortField(field, reverse, Type.STRING);
	}

	public SortOption(Label label, String value, SortField field)
	{
		super();
		this.label = label;
		this.value = value;
		this.field = field;
	}

	public String getValue()
	{
		return value;
	}

	public Label getLabel()
	{
		return label;
	}

	public SortField[] createSort()
	{
		return new SortField[]{field};
	}
}