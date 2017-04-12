package com.tle.web.lti.consumers.section;

import java.util.Collection;
import java.util.Collections;

import javax.inject.Inject;

import com.tle.common.lti.consumers.LtiConsumerConstants;
import com.tle.common.lti.consumers.entity.LtiConsumer;
import com.tle.core.lti.consumers.service.LtiConsumerService;
import com.tle.core.lti.consumers.service.session.LtiConsumerEditingBean;
import com.tle.core.services.entity.AbstractEntityService;
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

	@PlugKey("editor.pagetitle.new")
	private static Label NEW_CONSUMER_LABEL;
	@PlugKey("editor.pagetitle.edit")
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
