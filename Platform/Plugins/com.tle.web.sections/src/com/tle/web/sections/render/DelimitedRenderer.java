/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.web.sections.render;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;

public class DelimitedRenderer implements SectionRenderable
{
	private SectionRenderable delimiter;
	private SectionRenderable[] renderers;

	public DelimitedRenderer(String delimited, Object... objects)
	{
		this(new LabelRenderer(new TextLabel(delimited)), Arrays.asList(objects));
	}

	public DelimitedRenderer(SectionRenderable delimiter, Collection<?> objects)
	{
		this.delimiter = delimiter;
		this.renderers = SectionUtils.convertToRenderers(objects);
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(delimiter);
		info.preRender(renderers);
	}

	@Override
	public void realRender(SectionWriter writer) throws IOException
	{
		boolean first = true;
		for( SectionRenderable renderer : renderers )
		{
			if( !first )
			{
				writer.render(delimiter);
			}
			first = false;
			writer.render(renderer);
		}
	}

}
