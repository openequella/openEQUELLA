package com.tle.web.userscripts.section;

import java.util.Collection;
import java.util.Collections;

import javax.inject.Inject;

import com.tle.common.userscripts.UserScriptsConstants;
import com.tle.common.userscripts.entity.UserScript;
import com.tle.core.services.entity.AbstractEntityService;
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

	@PlugKey("editor.pagetitle.new")
	private static Label NEW_SCRIPT_LABEL;
	@PlugKey("editor.pagetitle.edit")
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
