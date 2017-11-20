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

package com.tle.web.userscripts.section;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.WordUtils;

import com.google.common.collect.Lists;
import com.tle.common.userscripts.UserScriptsConstants;
import com.tle.common.userscripts.entity.UserScript;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.security.TLEAclManager;
import com.tle.core.userscripts.service.UserScriptsService;
import com.tle.web.entities.section.AbstractShowEntitiesSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.component.model.SelectionsTableSelection;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;

@TreeIndexed
public class ShowUserScriptsSection
	extends
		AbstractShowEntitiesSection<UserScript, ShowUserScriptsSection.ShowUserScriptsModel>
{
	@PlugKey("scripts.settings.title")
	private static Label TITLE_LABEL;
	@PlugKey("scripts.add")
	private static Label ADD_SCRIPT_LABEL;
	@PlugKey("scripts.column.script")
	private static Label ENTITY_COLUMN_LABEL;
	@PlugKey("scripts.table.empty")
	private static Label NO_SCRIPTS_LABEL;
	@PlugKey("scripts.confirm.delete")
	private static Label DELETE_CONFIRM_LABEL;
	@PlugKey("scripts.column.type")
	private static Label COLUMN_TYPE_LABEL;
	@PlugKey("scripts.column.module")
	private static Label COLUMN_MODULE_NAME;

	@Inject
	private UserScriptsService userScriptsService;
	@Inject
	private TLEAclManager aclManager;

	@Override
	protected Label getTitleLabel(SectionInfo info)
	{
		return TITLE_LABEL;
	}

	@Override
	protected Label getAddLabel()
	{
		return ADD_SCRIPT_LABEL;
	}

	@Override
	protected Label getEntityColumnLabel()
	{
		return ENTITY_COLUMN_LABEL;
	}

	@Override
	protected Label getEmptyListLabel()
	{
		return NO_SCRIPTS_LABEL;
	}

	@Override
	protected boolean canAdd(SectionInfo info)
	{
		return !aclManager.filterNonGrantedPrivileges(UserScriptsConstants.PRIV_CREATE_SCRIPT).isEmpty();
	}

	@Override
	protected Label getDeleteConfirmLabel(SectionInfo info, UserScript entity)
	{
		return DELETE_CONFIRM_LABEL;
	}

	@Override
	protected boolean isInUse(SectionInfo info, UserScript entity)
	{
		return false;
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new ShowUserScriptsModel();
	}

	@Override
	protected AbstractEntityService<?, UserScript> getEntityService()
	{
		return userScriptsService;
	}

	@Override
	protected List<Object> getColumnHeadings()
	{
		return Lists.newArrayList((Object) getEntityColumnLabel(), (Object) COLUMN_MODULE_NAME,
			(Object) COLUMN_TYPE_LABEL);
	}

	@Override
	protected void addDynamicColumnData(SectionInfo info, UserScript script, SelectionsTableSelection row)
	{
		row.addColumn(new TextLabel(script.getModuleName()));
		row.addColumn(new TextLabel(WordUtils.capitalize(script.getScriptType().toLowerCase())));
	}

	public class ShowUserScriptsModel extends AbstractShowEntitiesSection.AbstractShowEntitiesModel
	{
	}

}
