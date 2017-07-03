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

package com.tle.common.htmleditor;

import java.util.ArrayList;
import java.util.List;

import com.tle.common.settings.ConfigurationProperties;
import com.tle.common.settings.annotation.Property;
import com.tle.common.settings.annotation.PropertyDataList;

/**
 * Making this class final overcomes some of Sonar's technical complaints about
 * Cloneable.clone
 * 
 * @author Aaron
 */
public final class HtmlEditorConfiguration implements ConfigurationProperties, Cloneable
{
	private static final long serialVersionUID = 1L;

	@PropertyDataList(key = "htmleditor.toolbar.rows", type = HtmlEditorToolbarConfig.class)
	private final List<HtmlEditorToolbarConfig> rows = new ArrayList<HtmlEditorToolbarConfig>();

	@Property(key = "htmleditor.editoroptions")
	private String editorOptions;

	@Property(key = "htmleditor.stylesheet.uuid")
	private String stylesheetUuid;

	public List<HtmlEditorToolbarConfig> getRows()
	{
		if( rows.size() < 3 )
		{
			synchronized( this )
			{
				if( rows.size() < 3 )
				{
					rows.add(new HtmlEditorToolbarConfig());
				}
			}
		}
		return rows;
	}

	public String getEditorOptions()
	{
		return editorOptions;
	}

	public void setEditorOptions(String editorOptions)
	{
		this.editorOptions = editorOptions;
	}

	public String getStylesheetUuid()
	{
		return stylesheetUuid;
	}

	public void setStylesheetUuid(String stylesheetUuid)
	{
		this.stylesheetUuid = stylesheetUuid;
	}

	// final class not calling super.clone, not returning Object
	@Override
	public HtmlEditorConfiguration clone() // NOSONAR
	{
		final HtmlEditorConfiguration clone = new HtmlEditorConfiguration();
		clone.setEditorOptions(editorOptions);
		clone.setStylesheetUuid(stylesheetUuid);
		for( HtmlEditorToolbarConfig row : rows )
		{
			final HtmlEditorToolbarConfig newRow = new HtmlEditorToolbarConfig();
			newRow.getButtons().addAll(row.getButtons());
			clone.rows.add(newRow);
		}
		return clone;
	}
}
