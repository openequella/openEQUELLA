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

package com.tle.web.itemlist;

import java.util.Collection;

import com.tle.web.sections.render.DelimitedRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.renderers.SpanRenderer;

public class DelimitedMetadata implements MetadataEntry
{
	private Label label;
	private SectionRenderable renderable;

	@SuppressWarnings("nls")
	public DelimitedMetadata(Label label, Collection<SectionRenderable> vals)
	{
		this.label = label;
		renderable = new SpanRenderer("itemresult-meta-delim", new DelimitedRenderer(
			new SpanRenderer("separator", "|"), vals));
	}

	@Override
	public Label getLabel()
	{
		return label;
	}

	@Override
	public SectionRenderable getValue()
	{
		return renderable;
	}
}
