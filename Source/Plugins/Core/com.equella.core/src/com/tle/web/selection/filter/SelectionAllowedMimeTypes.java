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

package com.tle.web.selection.filter;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.generic.CachedData.CacheFiller;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;

/**
 * @author aholland
 */
@Bind
@Singleton
public class SelectionAllowedMimeTypes implements CacheFiller<Collection<String>>
{
	@Inject
	private SelectionService selectionService;

	@Override
	public Collection<String> get(SectionInfo info)
	{
		SelectionSession session = selectionService.getCurrentSession(info);
		if( session != null )
		{
			SelectionFilter mimeFilter = (SelectionFilter) session.getAttribute(SelectionFilter.class);
			if( mimeFilter != null )
			{
				return mimeFilter.getAllowedMimeTypes();
			}
		}
		return null;
	}
}