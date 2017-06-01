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

package com.tle.web.portal.editor;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

public interface PortletEditor
{
	void create(SectionInfo info, String type, boolean admin);

	void edit(SectionInfo info, String portletUuid, boolean admin);

	SectionRenderable render(RenderContext info);

	void saveToSession(SectionInfo info);

	void loadFromSession(SectionInfo info);

	void restore(SectionInfo info);

	void register(SectionTree tree, String parentId);

	void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs);

	SectionRenderable renderHelp(RenderContext context);
}
