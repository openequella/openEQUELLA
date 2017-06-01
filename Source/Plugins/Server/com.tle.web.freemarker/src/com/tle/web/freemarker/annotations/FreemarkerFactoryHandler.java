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

package com.tle.web.freemarker.annotations;

import javax.inject.Singleton;

import com.google.inject.Inject;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.web.freemarker.CustomTemplateLoader;
import com.tle.web.sections.Section;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.registry.handler.CachedScannerHandler;

@Bind
@Singleton
public class FreemarkerFactoryHandler extends CachedScannerHandler<AnnotatedViewFactoryScanner>
{
	@Inject
	private CustomTemplateLoader templateLoader;
	@Inject
	private PluginService pluginService;

	@Override
	public void registered(String id, SectionTree tree, final Section section)
	{
		final AnnotatedViewFactoryScanner scanner = getForClass(section.getClass());
		scanner.setupFactories(section, templateLoader, pluginService);
	}

	@Override
	protected AnnotatedViewFactoryScanner newEntry(Class<?> clazz)
	{
		return new AnnotatedViewFactoryScanner(clazz, this);
	}

}
