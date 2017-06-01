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

package com.tle.web.remoterepo.sru;

import com.tle.web.search.guice.AbstractSearchModule;

@SuppressWarnings("nls")
public class SRUModule extends AbstractSearchModule
{
	private static String SRU_TREE = "sruTree";

	@Override
	protected NodeProvider getRootNode()
	{
		return node(SruRootRemoteRepoSection.class);
	}

	@Override
	protected NodeProvider getQueryNode()
	{
		return node(SruQuerySection.class);
	}

	@Override
	protected NodeProvider getResultsNode()
	{
		return node(SruResultsSection.class);
	}

	@Override
	protected String getTreeName()
	{
		return SRU_TREE;
	}
}
