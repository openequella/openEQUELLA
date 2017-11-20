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

package com.tle.web.wizard.render;

import java.io.Writer;
import java.util.Map;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.freemarker.FreemarkerSectionResult;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.equella.freemarker.ExtendedFreemarkerFactory;
import com.tle.web.wizard.controls.WebControl;

@NonNullByDefault
public class WizardExtendedFactory extends ExtendedFreemarkerFactory
{
	@Override
	protected void addRootObjects(Map<String, Object> map, FreemarkerSectionResult result, Writer writer)
	{
		super.addRootObjects(map, result, writer);
		SectionId sectionId = result.getSectionId();
		if( sectionId != null && writer instanceof SectionWriter )
		{
			SectionId section = ((SectionWriter) writer).getSectionForId(sectionId);
			if( section instanceof WebControl )
			{
				WebControl webControl = (WebControl) section;
				map.put("wc", webControl.getWrappedControl()); //$NON-NLS-1$
				map.put("c", webControl); //$NON-NLS-1$
				map.put("fid", webControl.getFormName()); //$NON-NLS-1$
			}
		}
	}
}
