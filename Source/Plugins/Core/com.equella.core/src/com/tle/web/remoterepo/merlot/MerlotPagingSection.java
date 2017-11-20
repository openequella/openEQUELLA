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

package com.tle.web.remoterepo.merlot;

import com.tle.core.fedsearch.RemoteRepoSearchResult;
import com.tle.web.remoterepo.event.RemoteRepoSearchResultEvent;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.search.PagingSection;

public class MerlotPagingSection
	extends
		PagingSection<MerlotRemoteRepoSearchEvent, RemoteRepoSearchResultEvent<RemoteRepoSearchResult>>
{
	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		this.setRenderScreenOptions(false);
	}
}
