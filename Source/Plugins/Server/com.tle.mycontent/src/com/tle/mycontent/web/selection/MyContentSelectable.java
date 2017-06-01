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

package com.tle.mycontent.web.selection;

import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionsController;
import com.tle.web.selection.SelectableInterface;
import com.tle.web.selection.SelectionSession;

/**
 * @author aholland
 */
@Bind
@Singleton
@SuppressWarnings("nls")
public class MyContentSelectable implements SelectableInterface
{
	private static final String SELECTABLE_ID = "mycontent";

	@Inject
	private SectionsController controller;

	@Override
	public SectionInfo createSectionInfo(SectionInfo info, SelectionSession session)
	{
		// Should there ever be one, it should be the only one available
		session.setAllowedSelectNavActions(Collections.singleton(SELECTABLE_ID));
		SectionInfo newInfo = getSearchTree(info);
		return newInfo; // NOSONAR (keeping local variable for readability)
	}

	protected SectionInfo getSearchTree(SectionInfo info)
	{
		return controller.createForward(info, "/access/mycontentselect.do");
	}
}
