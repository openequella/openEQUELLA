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

package com.tle.web.mimetypes.guice;

import com.google.inject.name.Names;
import com.tle.web.mimetypes.search.section.AddMimeAction;
import com.tle.web.mimetypes.search.section.MimeResultsSection;
import com.tle.web.mimetypes.search.section.RootMimeSection;
import com.tle.web.mimetypes.section.MimeTypesEditSection;
import com.tle.web.search.filter.SimpleResetFiltersQuerySection;
import com.tle.web.search.guice.AbstractSearchModule;

@SuppressWarnings("nls")
public class MimeModule extends AbstractSearchModule
{
	@Override
	protected void configure()
	{
		super.configure();
		bind(Object.class).annotatedWith(Names.named("/access/mimeedit")).toProvider(node(MimeTypesEditSection.class));
	}

	@Override
	protected NodeProvider getRootNode()
	{
		return node(RootMimeSection.class);
	}

	@Override
	protected NodeProvider getQueryNode()
	{
		return node(SimpleResetFiltersQuerySection.class);
	}

	@Override
	protected NodeProvider getResultsNode()
	{
		return node(MimeResultsSection.class);
	}

	@Override
	protected void addActions(NodeProvider node)
	{
		node.child(AddMimeAction.class);
	}

	@Override
	protected String getTreeName()
	{
		return "/access/mime";
	}
}
