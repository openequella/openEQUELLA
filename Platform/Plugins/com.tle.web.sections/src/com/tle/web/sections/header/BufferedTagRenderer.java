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

package com.tle.web.sections.header;

import java.io.IOException;

import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.render.TagState;

public class BufferedTagRenderer extends TagRenderer
{
	public BufferedTagRenderer(String tag, TagState state)
	{
		super(tag, state);
	}

	private String nested;

	@Override
	public void realRender(SectionWriter writer) throws IOException
	{
		SectionRenderable renderable = getNestedRenderable();
		nested = SectionUtils.renderToString(writer, renderable);
		super.realRender(writer);
	}

	@Override
	protected void writeMiddle(SectionWriter writer) throws IOException
	{
		writer.write(nested);
	}
}
