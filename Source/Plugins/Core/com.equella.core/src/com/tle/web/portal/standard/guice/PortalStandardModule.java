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

package com.tle.web.portal.standard.guice;

import com.google.inject.Scopes;
import com.google.inject.name.Names;
import com.tle.web.freemarker.BasicFreemarkerFactory;
import com.tle.web.portal.standard.renderer.SearchPortletRenderer;
import com.tle.web.sections.equella.guice.SectionsModule;

public class PortalStandardModule extends SectionsModule
{

	@SuppressWarnings("nls")
	@Override
	protected void configure()
	{
		NodeProvider node = node(PortalSearchWrapper.class);
		node.child(SearchPortletRenderer.class);
		bind(Object.class).annotatedWith(Names.named("com.tle.web.portal.standard.searchSelectionPortal")).toProvider(
			node);
	}

}
