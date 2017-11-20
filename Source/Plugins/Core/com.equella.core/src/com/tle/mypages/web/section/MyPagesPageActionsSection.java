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

package com.tle.mypages.web.section;

import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.js.generic.OverrideHandler;

public class MyPagesPageActionsSection extends AbstractMyPagesPageActionsSection
{
	@AjaxFactory
	private AjaxGenerator ajax;

	@SuppressWarnings("nls")
	@Override
	protected void setupAddHandler(SectionTree tree)
	{
		addPage.setClickHandler(new OverrideHandler(ajax.getAjaxUpdateDomFunction(tree, contribSection,
			events.getEventHandler("addPage"), "page-edit", "pages-table-ajax")));
	}

}
