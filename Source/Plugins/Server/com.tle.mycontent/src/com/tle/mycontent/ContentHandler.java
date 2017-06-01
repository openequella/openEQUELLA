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

package com.tle.mycontent;

import java.util.List;

import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.ItemId;
import com.tle.web.itemlist.item.StandardItemListEntry;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

/**
 * @author aholland
 */
public interface ContentHandler
{
	HtmlLinkState decorate(SectionInfo info, StandardItemListEntry itemEntry);

	void contribute(SectionInfo info, ItemDefinition collection);

	boolean canEdit(SectionInfo info, ItemId id);

	void edit(SectionInfo info, ItemId id);

	SectionRenderable render(RenderContext context);

	List<HtmlComponentState> getMajorActions(RenderContext context);

	List<HtmlComponentState> getMinorActions(RenderContext context);

	Label getTitle(SectionInfo info);

	void addTrees(SectionInfo info, boolean parameters);

	boolean isRawFiles();

	void addCrumbs(SectionInfo info, Decorations decorations, Breadcrumbs crumbs);
}
