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

package com.tle.web.manualdatafixes;

import com.google.inject.name.Names;
import com.tle.web.manualdatafixes.fixes.AttachmentHashOpSection;
import com.tle.web.manualdatafixes.fixes.GenerateThumbnailOpSection;
import com.tle.web.manualdatafixes.fixes.ReindexOpSection;
import com.tle.web.sections.equella.guice.SectionsModule;

@SuppressWarnings("nls")
public class ManualDataFixesModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("/access/manualdatafixes")).toProvider(
			node(RootManualDataFixesSettingsSection.class).placeHolder("FIXES").children(
				GenerateThumbnailOpSection.class, AttachmentHashOpSection.class, ReindexOpSection.class));
	}
}
