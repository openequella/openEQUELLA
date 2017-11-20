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

package com.tle.web.lti.consumers.section;

import java.util.Collection;
import java.util.Collections;

import javax.inject.Inject;

import com.tle.common.lti.consumers.LtiConsumerConstants;
import com.tle.common.lti.consumers.entity.LtiConsumer;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.lti.consumers.service.LtiConsumerService;
import com.tle.core.lti.consumers.service.session.LtiConsumerEditingBean;
import com.tle.web.entities.section.AbstractEntityContributeSection;
import com.tle.web.entities.section.EntityEditor;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;

@TreeIndexed
public class LtiConsumersContributeSection
	extends
		AbstractEntityContributeSection<LtiConsumerEditingBean, LtiConsumer, LtiConsumersContributeSection.LtiConsumerContributeModel>
{

	@PlugKey("lti.editor.pagetitle.new")
	private static Label NEW_CONSUMER_LABEL;
	@PlugKey("lti.editor.pagetitle.edit")
	private static Label EDIT_CONSUMER_LABEL;

	@Inject
	private LtiConsumerService ltiConsumerService;
	@Inject
	private LtiConsumerEditorSection editorSection;

	@Override
	protected AbstractEntityService<LtiConsumerEditingBean, LtiConsumer> getEntityService()
	{
		return ltiConsumerService;
	}

	@Override
	protected Label getCreatingLabel(SectionInfo info)
	{
		return NEW_CONSUMER_LABEL;
	}

	@Override
	protected Label getEditingLabel(SectionInfo info)
	{
		return EDIT_CONSUMER_LABEL;
	}

	@Override
	protected EntityEditor<LtiConsumerEditingBean, LtiConsumer> getEditor(SectionInfo info)
	{
		return editorSection;
	}

	@Override
	protected String getCreatePriv()
	{
		return LtiConsumerConstants.PRIV_CREATE_CONUSMER;
	}

	@Override
	protected String getEditPriv()
	{
		return LtiConsumerConstants.PRIV_EDIT_CONSUMER;
	}

	@Override
	protected Collection<EntityEditor<LtiConsumerEditingBean, LtiConsumer>> getAllEditors()
	{
		return Collections.singletonList((EntityEditor<LtiConsumerEditingBean, LtiConsumer>) editorSection);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new LtiConsumerContributeModel();
	}

	public class LtiConsumerContributeModel
		extends
			AbstractEntityContributeSection<LtiConsumerEditingBean, LtiConsumer, LtiConsumerContributeModel>.EntityContributeModel
	{
		// Empty
	}

}
