package com.tle.web.hierarchy.portlet.editor;

import java.util.Map;

import com.tle.common.portal.entity.Portlet;
import com.tle.core.guice.Bind;
import com.tle.core.portal.service.PortletEditingBean;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.portal.editor.AbstractPortletEditorSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.SectionRenderable;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
@Bind
public class BrowsePortletEditorSection
	extends
		AbstractPortletEditorSection<AbstractPortletEditorSection.AbstractPortletEditorModel>
{
	private static final String TYPE = "browse";

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Override
	public String getDefaultPropertyName()
	{
		return "ebrs";
	}

	@Override
	protected Portlet createNewPortlet()
	{
		return new Portlet(TYPE);
	}

	@Override
	protected void customClear(SectionInfo info)
	{
		// Nothing to do here
	}

	@Override
	protected void customLoad(SectionInfo info, PortletEditingBean portlet)
	{
		// Nothing to do here
	}

	@Override
	protected SectionRenderable customRender(RenderEventContext context,
		AbstractPortletEditorSection.AbstractPortletEditorModel model, PortletEditingBean portlet) throws Exception
	{
		return null;
	}

	@Override
	protected void customSave(SectionInfo info, PortletEditingBean portlet)
	{
		// Nothing to do here
	}

	@Override
	protected void customValidate(SectionInfo info, PortletEditingBean portlet, Map<String, Object> errors)
	{
		// Nothing to do here
	}

	@Override
	public Class<AbstractPortletEditorSection.AbstractPortletEditorModel> getModelClass()
	{
		return AbstractPortletEditorSection.AbstractPortletEditorModel.class;
	}

	@Override
	public SectionRenderable renderHelp(RenderContext context)
	{
		return viewFactory.createResult("portlet/browseportleteditorhelp.ftl", this);
	}
}
