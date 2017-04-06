package com.tle.web.myresources.portal;

import java.util.Map;

import com.tle.common.portal.entity.Portlet;
import com.tle.core.guice.Bind;
import com.tle.core.portal.service.PortletEditingBean;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.portal.editor.AbstractPortletEditorSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.SimpleSectionResult;

/**
 * @author aholland
 */
@Bind
@SuppressWarnings("nls")
public class MyResourcesPortletEditor
	extends
		AbstractPortletEditorSection<AbstractPortletEditorSection.AbstractPortletEditorModel>
{
	private static final String TYPE = "myresources";

	@ViewFactory
	private FreemarkerFactory viewFactory;

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
		return new SimpleSectionResult("");
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
	public void register(SectionTree tree, String parentId)
	{
		tree.registerSections(this, parentId);
	}

	@Override
	public SectionRenderable render(RenderContext info)
	{
		return renderSection(info, this);
	}

	@Override
	public void restore(SectionInfo info)
	{
		// nothing
	}

	@Override
	public SectionRenderable renderHelp(RenderContext context)
	{
		return viewFactory.createResult("portal/myresourceseditorhelp.ftl", this);
	}
}
