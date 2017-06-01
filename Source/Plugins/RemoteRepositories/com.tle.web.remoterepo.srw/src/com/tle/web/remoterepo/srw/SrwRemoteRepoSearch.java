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

package com.tle.web.remoterepo.srw;

import javax.inject.Singleton;

import com.tle.beans.entity.FederatedSearch;
import com.tle.beans.search.SRWSettings;
import com.tle.beans.search.SearchSettings;
import com.tle.core.guice.Bind;
import com.tle.web.remoterepo.impl.AbstractRemoteRepoSearch;

/**
 * @author aholland
 */
@Bind
@Singleton
public class SrwRemoteRepoSearch extends AbstractRemoteRepoSearch
{
	@Override
	protected String getTreePath()
	{
		return "/access/srw.do"; //$NON-NLS-1$
	}

	@Override
	public SearchSettings createSettings(FederatedSearch search)
	{
		SRWSettings settings = new SRWSettings();
		settings.load(search);
		return settings;
	}

	@Override
	public String getContextKey()
	{
		return SrwRootRemoteRepoSection.CONTEXT_KEY;
	}
}
