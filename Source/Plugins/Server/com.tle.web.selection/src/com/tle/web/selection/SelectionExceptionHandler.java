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

package com.tle.web.selection;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.AbstractModalSessionExceptionHandler;
import com.tle.web.selection.section.RootSelectionSection;

@Bind
@Singleton
public class SelectionExceptionHandler extends AbstractModalSessionExceptionHandler<SelectionSession>
{
	@Inject
	private SelectionService selectionService;

	@Override
	protected SelectionServiceImpl getModalService()
	{
		return (SelectionServiceImpl) selectionService;
	}

	@Override
	protected boolean shouldHandle(SectionInfo info)
	{
		RootSelectionSection rootSection = info.lookupSection(RootSelectionSection.class);
		return !rootSection.getModel(info).isRendering();
	}
}
