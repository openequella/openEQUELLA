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

package com.tle.web.htmleditor;

import java.io.IOException;

import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;

/**
 * @author Aaron
 */
public class HtmlEditorButtonDefinition implements SectionRenderable
{
	private final String id;
	private final String pluginId;
	private final SectionRenderable renderable;
	private final int defaultRow;
	private final boolean singleton;
	private final Label label;

	public HtmlEditorButtonDefinition(String id, String pluginId, SectionRenderable renderable, Label label,
		int defaultRow, boolean singleton)
	{
		this.id = id;
		this.pluginId = pluginId;
		this.renderable = renderable;
		this.label = label;
		this.defaultRow = defaultRow;
		this.singleton = singleton;
	}

	public HtmlEditorButtonDefinition(String id, String pluginId, SectionRenderable renderable, Label label)
	{
		this(id, pluginId, renderable, label, -1, true);
	}

	public String getId()
	{
		return id;
	}

	public String getPluginId()
	{
		return pluginId;
	}

	public SectionRenderable getRenderable()
	{
		return renderable;
	}

	public Label getLabel()
	{
		return label;
	}

	// Friggen javadoc formatting. Worst. Idea. Ever.
	/**
	 * CURRENTLY UNUSED: reason is that the default button configuration is
	 * ordered AND contains multiple separator buttons. <br>
	 * A default row of -1 means the button does not show on the toolbar by
	 * default
	 * 
	 * @return
	 */
	public int getDefaultRow()
	{
		return defaultRow;
	}

	/**
	 * Ie. only one instance of this button is allowed on the toolbar.
	 * 
	 * @return
	 */
	public boolean isSingleton()
	{
		return singleton;
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		if( renderable != null )
		{
			renderable.preRender(info);
		}
	}

	@Override
	public void realRender(SectionWriter writer) throws IOException
	{
		if( renderable != null )
		{
			renderable.realRender(writer);
		}
	}
}
