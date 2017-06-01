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

package com.tle.web.myresource;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.ItemId;
import com.tle.core.guice.Bind;
import com.tle.mycontent.SimpleContentHandler;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

/**
 * @author jmaginnis
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class MyResourceHandler extends SimpleContentHandler<MyResourceContributeSection>
{
	public static final String HANDLER_ID = "myresource";

	private static final Label TITLE = new KeyLabel(ResourcesService.getResourceHelper(MyResourceHandler.class).key(
		"handlerName"));

	@Inject
	private Provider<SectionTree> handlerTree;

	@Override
	protected SectionTree createTree()
	{
		return handlerTree.get();
	}

	@Override
	public Label getTitle(SectionInfo info)
	{
		return TITLE;
	}

	@Override
	protected void doContribute(MyResourceContributeSection contribute, SectionInfo info, ItemDefinition collection)
	{
		// Nothing
	}

	@Override
	protected void doEdit(MyResourceContributeSection contribute, SectionInfo info, ItemId id)
	{
		contribute.edit(info, id);
	}

	@Override
	public Class<MyResourceContributeSection> getContributeSectionClass()
	{
		return MyResourceContributeSection.class;
	}

	@Override
	public boolean isRawFiles()
	{
		return true;
	}

	@Override
	public void doAddCrumbs(MyResourceContributeSection contribute, SectionInfo info, Decorations decorations,
		Breadcrumbs crumbs)
	{
		contribute.addCrumbs(info, decorations, crumbs);
	}
}
