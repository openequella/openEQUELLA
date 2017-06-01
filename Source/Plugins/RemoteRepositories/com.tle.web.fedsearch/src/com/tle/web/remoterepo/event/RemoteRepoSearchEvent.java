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

package com.tle.web.remoterepo.event;

import com.tle.beans.entity.FederatedSearch;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.search.event.AbstractSearchEvent;
import com.tle.web.sections.equella.search.event.SearchEventListener;

/**
 * @author aholland
 */
public abstract class RemoteRepoSearchEvent<E extends RemoteRepoSearchEvent<E>> extends AbstractSearchEvent<E>
{
	private final FederatedSearch search;

	protected RemoteRepoSearchEvent(SectionId sectionId, FederatedSearch search)
	{
		super(sectionId);
		this.search = search;
	}

	@Override
	public void fire(SectionId sectionId, SectionInfo info, SearchEventListener<E> listener) throws Exception
	{
		listener.prepareSearch(info, (E) this);
	}

	public FederatedSearch getSearch()
	{
		return search;
	}
}
