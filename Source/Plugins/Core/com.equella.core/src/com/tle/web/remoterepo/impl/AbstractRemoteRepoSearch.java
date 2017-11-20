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

package com.tle.web.remoterepo.impl;

import com.tle.beans.entity.FederatedSearch;
import com.tle.web.remoterepo.RemoteRepoSearch;
import com.tle.web.remoterepo.RemoteRepoSection;
import com.tle.web.sections.SectionInfo;

/**
 * @author aholland
 */
public abstract class AbstractRemoteRepoSearch implements RemoteRepoSearch
{
	@Override
	public void forward(SectionInfo info, FederatedSearch search)
	{
		SectionInfo forward = info.createForward(getTreePath());

		RemoteRepoSection results = forward.lookupSection(RemoteRepoSection.class);
		results.setSearchUuid(forward, search.getUuid());

		info.forwardAsBookmark(forward);
	}

	protected abstract String getTreePath();
}
