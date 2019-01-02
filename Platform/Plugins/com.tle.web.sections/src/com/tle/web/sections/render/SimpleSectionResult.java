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

package com.tle.web.sections.render;

import java.io.IOException;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.Check;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;

@NonNullByDefault
public class SimpleSectionResult implements SectionRenderable
{
	private final Object result;
	private final String responseMimeType;

	public SimpleSectionResult(@Nullable Object result)
	{
		this(result, null);
	}

	public SimpleSectionResult(@Nullable Object result, @Nullable String responseMimeType)
	{
		this.result = (result == null ? "" : result); //$NON-NLS-1$
		this.responseMimeType = responseMimeType;
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		if( !Check.isEmpty(responseMimeType) )
		{
			info.getResponse().setContentType(responseMimeType);
		}
	}

	@Override
	public void realRender(SectionWriter writer) throws IOException
	{
		writer.write(result.toString());
	}

}
