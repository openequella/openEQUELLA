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

package com.tle.web.sections.standard.renderers;

import java.io.IOException;
import java.util.Map;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.js.ElementId;

@SuppressWarnings("nls")
@NonNullByDefault
public class LabelTagRenderer extends DivRenderer
{
	@Nullable
	private final ElementId labelFor;

	public LabelTagRenderer(@Nullable ElementId labelFor, @Nullable String styleClass, Object text)
	{
		super("label", styleClass, text);
		this.labelFor = labelFor;
	}

	@Override
	protected Map<String, String> prepareAttributes(SectionWriter writer) throws IOException
	{
		Map<String, String> attrs = super.prepareAttributes(writer);
		if( labelFor != null )
		{
			attrs.put("for", labelFor.getElementId(writer));
		}
		return attrs;
	}
}
