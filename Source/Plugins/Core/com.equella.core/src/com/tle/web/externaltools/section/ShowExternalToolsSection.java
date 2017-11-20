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

package com.tle.web.externaltools.section;

import javax.inject.Inject;

import com.tle.common.externaltools.constants.ExternalToolConstants;
import com.tle.common.externaltools.entity.ExternalTool;
import com.tle.common.i18n.LangUtils;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.externaltools.service.ExternalToolsService;
import com.tle.core.security.TLEAclManager;
import com.tle.web.entities.section.AbstractShowEntitiesSection;
import com.tle.web.externaltools.section.ShowExternalToolsSection.ShowExternalToolsModel;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.result.util.KeyLabel;

@TreeIndexed
public class ShowExternalToolsSection extends AbstractShowEntitiesSection<ExternalTool, ShowExternalToolsModel>
{
	@PlugKey("tools.settings.title")
	private static Label TITLE_LABEL;
	@PlugKey("tools.add")
	private static Label ADD_LABEL;
	@PlugKey("tools.table.empty")
	private static Label EMPTY_LABEL;
	@PlugKey("tools.columns.entity")
	private static Label ENTITY_LABEL;
	@PlugKey("tools.confirm.delete")
	private static String KEY_DELETE_CONFIRM;

	@Inject
	private TLEAclManager aclManager;
	@Inject
	private ExternalToolsService toolService;
	@ViewFactory
	private FreemarkerFactory view;

	@Override
	protected AbstractEntityService<?, ExternalTool> getEntityService()
	{
		return toolService;
	}

	@Override
	protected Label getTitleLabel(SectionInfo info)
	{
		return TITLE_LABEL;
	}

	@Override
	protected Label getAddLabel()
	{
		return ADD_LABEL;
	}

	@Override
	protected Label getEntityColumnLabel()
	{
		return ENTITY_LABEL;
	}

	@Override
	protected Label getEmptyListLabel()
	{
		return EMPTY_LABEL;
	}

	@Override
	protected Label getDeleteConfirmLabel(SectionInfo info, ExternalTool tool)
	{
		return new KeyLabel(KEY_DELETE_CONFIRM, LangUtils.getString(tool.getName()));
	}

	@Override
	protected boolean canAdd(SectionInfo info)
	{
		return !aclManager.filterNonGrantedPrivileges(ExternalToolConstants.PRIV_CREATE_TOOL).isEmpty();

	}

	@Override
	protected SectionRenderable renderTop(RenderEventContext context)
	{
		return view.createResult("toolstop.ftl", context);
	}

	@Override
	protected boolean isInUse(SectionInfo info, ExternalTool entity)
	{
		return false;
	}

	@Override
	protected boolean canClone(SectionInfo info, ExternalTool ent)
	{
		return false;
	}

	public class ShowExternalToolsModel extends AbstractShowEntitiesSection.AbstractShowEntitiesModel
	{
	}
}
