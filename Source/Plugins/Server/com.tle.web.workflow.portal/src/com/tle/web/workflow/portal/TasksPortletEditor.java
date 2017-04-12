package com.tle.web.workflow.portal;

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
import com.tle.web.sections.render.SimpleSectionResult;

/**
 * @author aholland / agibb
 */
@Bind
public class TasksPortletEditor
	extends
		AbstractPortletEditorSection<TasksPortletEditor.WorkflowTasksPortletEditorModel>
{
	private static final String TYPE = "tasks";

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Override
	protected SectionRenderable customRender(RenderEventContext context, WorkflowTasksPortletEditorModel model,
		PortletEditingBean portlet) throws Exception
	{
		return new SimpleSectionResult("");
	}

	@Override
	protected Portlet createNewPortlet()
	{
		return new Portlet(TYPE);
	}

	@Override
	protected void customLoad(SectionInfo info, PortletEditingBean portlet)
	{
		// Nothing by default
	}

	@Override
	protected void customSave(SectionInfo info, PortletEditingBean portlet)
	{
		// Nothing by default
	}

	@Override
	protected void customClear(SectionInfo info)
	{
		// Nothing by default
	}

	@Override
	protected void customValidate(SectionInfo info, PortletEditingBean portlet, Map<String, Object> errors)
	{
		// Nothing by default
	}

	@Override
	public Class<WorkflowTasksPortletEditorModel> getModelClass()
	{
		return WorkflowTasksPortletEditorModel.class;
	}

	public static class WorkflowTasksPortletEditorModel extends AbstractPortletEditorSection.AbstractPortletEditorModel
	{
		// Nothing by default
	}

	@Override
	public SectionRenderable renderHelp(RenderContext context)
	{
		return viewFactory.createResult("portal/tasksportleteditorhelp.ftl", this);
	}
}
