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

import java.util.Collection;
import java.util.Collections;

import javax.inject.Inject;

import com.tle.common.userscripts.UserScriptsConstants;
import com.tle.common.userscripts.entity.UserScript;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.userscripts.service.UserScriptsService;
import com.tle.core.userscripts.service.session.UserScriptEditingBean;
import com.tle.web.entities.section.AbstractEntityContributeSection;
import com.tle.web.entities.section.EntityEditor;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;

@TreeIndexed
public class UserScriptContributeSection
	extends
		AbstractEntityContributeSection<UserScriptEditingBean, UserScript, UserScriptContributeSection.UserScriptContributeModel>
{

	@PlugKey("scripts.editor.pagetitle.new")
	private static Label NEW_SCRIPT_LABEL;
	@PlugKey("scripts.editor.pagetitle.edit")
	private static Label EDIT_SCRIPT_LABEL;

	@Inject
	private UserScriptEditorSection userScriptEditorSection;
	@Inject
	private UserScriptsService userScriptService;

	@Override
	protected AbstractEntityService<UserScriptEditingBean, UserScript> getEntityService()
	{
		return userScriptService;
	}

	@Override
	protected Label getCreatingLabel(SectionInfo info)
	{
		return NEW_SCRIPT_LABEL;
	}

	@Override
	protected Label getEditingLabel(SectionInfo info)
	{
		return EDIT_SCRIPT_LABEL;
	}

	@Override
	protected EntityEditor<UserScriptEditingBean, UserScript> getEditor(SectionInfo info)
	{
		return userScriptEditorSection;
	}

	@Override
	protected String getCreatePriv()
	{
		return UserScriptsConstants.PRIV_CREATE_SCRIPT;
	}

	@Override
	protected String getEditPriv()
	{

		return UserScriptsConstants.PRIV_EDIT_SCRIPT;
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new UserScriptContributeModel();
	}

	@Override
	protected Collection<EntityEditor<UserScriptEditingBean, UserScript>> getAllEditors()
	{
		return Collections.singletonList((EntityEditor<UserScriptEditingBean, UserScript>) userScriptEditorSection);
	}

	public class UserScriptContributeModel
		extends
			AbstractEntityContributeSection<UserScriptEditingBean, UserScript, UserScriptContributeModel>.EntityContributeModel
	{
		// Empty
	}
}
