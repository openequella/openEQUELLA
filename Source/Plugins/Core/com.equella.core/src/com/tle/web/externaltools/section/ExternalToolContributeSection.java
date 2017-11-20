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

import java.util.Collection;
import java.util.Collections;

import javax.inject.Inject;

import com.tle.common.externaltools.constants.ExternalToolConstants;
import com.tle.common.externaltools.entity.ExternalTool;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.externaltools.service.ExternalToolsService;
import com.tle.core.service.session.ExternalToolEditingBean;
import com.tle.web.entities.section.AbstractEntityContributeSection;
import com.tle.web.entities.section.EntityEditor;
import com.tle.web.externaltools.section.ExternalToolContributeSection.ExternalToolContributeModel;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;

@TreeIndexed
public class ExternalToolContributeSection
	extends
		AbstractEntityContributeSection<ExternalToolEditingBean, ExternalTool, ExternalToolContributeModel>
{
	@PlugKey("editor.pagetitle.new")
	private static Label NEW_SCRIPT_LABEL;
	@PlugKey("editor.pagetitle.edit")
	private static Label EDIT_SCRIPT_LABEL;

	@Inject
	private ExternalToolEditorSection toolEditorSection;
	@Inject
	private ExternalToolsService toolService;

	@Override
	protected AbstractEntityService<ExternalToolEditingBean, ExternalTool> getEntityService()
	{
		return toolService;
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
	protected EntityEditor<ExternalToolEditingBean, ExternalTool> getEditor(SectionInfo info)
	{
		return toolEditorSection;
	}

	@Override
	protected String getCreatePriv()
	{
		return ExternalToolConstants.PRIV_CREATE_TOOL;
	}

	@Override
	protected String getEditPriv()
	{
		return ExternalToolConstants.PRIV_EDIT_TOOL;
	}

	@Override
	protected Collection<EntityEditor<ExternalToolEditingBean, ExternalTool>> getAllEditors()
	{
		return Collections.singletonList((EntityEditor<ExternalToolEditingBean, ExternalTool>) toolEditorSection);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new ExternalToolContributeModel();
	}

	public class ExternalToolContributeModel
		extends
			AbstractEntityContributeSection<ExternalToolEditingBean, ExternalTool, ExternalToolContributeModel>.EntityContributeModel
	{
		// Empty
	}
}
