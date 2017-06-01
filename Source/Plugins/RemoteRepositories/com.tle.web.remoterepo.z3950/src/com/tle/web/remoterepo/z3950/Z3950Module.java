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

package com.tle.web.remoterepo.z3950;

import com.tle.web.search.guice.AbstractSearchModule;

@SuppressWarnings("nls")
public class Z3950Module extends AbstractSearchModule
{
	@Override
	protected NodeProvider getRootNode()
	{
		return node(Z3950RootRemoteRepoSection.class);
	}

	@Override
	protected NodeProvider getQueryNode()
	{
		return node(Z3950QuerySection.class);
	}

	@Override
	protected NodeProvider getResultsNode()
	{
		return node(Z3950ResultsSection.class);
	}

	@Override
	protected NodeProvider getPagingNode()
	{
		return node(Z3950PagingSection.class);
	}

	@Override
	protected String getTreeName()
	{
		return "z3950Tree";
	}
}
