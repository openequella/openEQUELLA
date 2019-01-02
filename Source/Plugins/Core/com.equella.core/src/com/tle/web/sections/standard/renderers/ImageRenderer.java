/*
 * Copyright 2019 Apereo
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

package com.tle.web.sections.standard.renderers;

import java.io.IOException;
import java.util.Map;

import com.tle.common.Check;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.render.TagState;

public class ImageRenderer extends TagRenderer
{
	private String source;
	private String height;
	private String width;
	private Label alt;

	/**
	 * Yes, the alt is mandatory. Don't make me unleash the w3c on your ass!
	 * 
	 * @param source
	 * @param alt
	 */
	public ImageRenderer(String source, Label alt)
	{
		this(new TagState(), source, alt);
	}

	public ImageRenderer(Bookmark source, Label alt)
	{
		this(new TagState(), source.getHref(), alt);
	}

	public ImageRenderer(TagState state, String source, Label alt)
	{
		super("img", state); //$NON-NLS-1$
		this.source = source;
		this.alt = alt;
	}

	public ImageRenderer setAlt(Label alt)
	{
		this.alt = alt;
		return this;
	}

	public Label getAlt()
	{
		return alt;
	}

	public String getSource()
	{
		return source;
	}

	public ImageRenderer setSource(String source)
	{
		this.source = source;
		return this;
	}

	public String getHeight()
	{
		return height;
	}

	public ImageRenderer setHeight(String height)
	{
		if( !Check.isEmpty(height) )
		{
			this.height = height;
		}
		return this;
	}

	public String getWidth()
	{
		return width;
	}

	public ImageRenderer setWidth(String width)
	{
		if( !Check.isEmpty(width) )
		{
			this.width = width;
		}
		return this;
	}

	@Override
	protected void prepareFirstAttributes(SectionWriter writer, Map<String, String> attrs) throws IOException
	{
		super.prepareFirstAttributes(writer, attrs);
		attrs.put("src", source); //$NON-NLS-1$
		attrs.put("width", width); //$NON-NLS-1$
		attrs.put("height", height); //$NON-NLS-1$
		if( alt != null )
		{
			attrs.put("alt", alt.getText()); //$NON-NLS-1$
			attrs.put("title", alt.getText()); //$NON-NLS-1$
		}
	}

	@Override
	protected void writeEnd(SectionWriter writer) throws IOException
	{
		// nothing
	}

	@Override
	protected void writeMiddle(SectionWriter writer) throws IOException
	{
		// nothing
	}
}
