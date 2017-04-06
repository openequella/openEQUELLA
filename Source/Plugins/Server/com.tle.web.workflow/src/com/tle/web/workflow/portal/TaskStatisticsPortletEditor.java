package com.tle.web.workflow.portal;

import java.util.Map;

import com.tle.common.Check;
import com.tle.common.portal.entity.Portlet;
import com.tle.core.guice.Bind;
import com.tle.core.portal.service.PortletEditingBean;
import com.tle.core.services.TaskStatisticsService.Trend;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.portal.editor.AbstractPortletEditorSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.listmodel.EnumListModel;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;

@SuppressWarnings("nls")
@Bind
public class TaskStatisticsPortletEditor
	extends
		AbstractPortletEditorSection<TaskStatisticsPortletEditor.TaskStatisticsPortletEditorModel>
{
	private static final String TYPE = "taskstatistics";
	private static final String KEY_DEFAULT_TREND = "trend";

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@PlugKey("portal.taskstats.trend.")
	private static String PREFIX;

	@Component(name = "tr", stateful = false)
	private SingleSelectionList<Trend> trend;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		trend.setListModel(new EnumListModel<Trend>(PREFIX, Trend.values()));
	}

	@Override
	protected Portlet createNewPortlet()
	{
		return new Portlet(TYPE);
	}

	@Override
	protected SectionRenderable customRender(RenderEventContext context, TaskStatisticsPortletEditorModel model,
		PortletEditingBean portlet) throws Exception
	{
		return viewFactory.createResult("portal/edit/edittaskstatistics.ftl", context);
	}

	@Override
	protected void customValidate(SectionInfo info, PortletEditingBean portlet, Map<String, Object> errors)
	{
		// Nothing
	}

	@Override
	protected void customLoad(SectionInfo info, PortletEditingBean portlet)
	{
		String defaultTrend = portlet.getAttribute(KEY_DEFAULT_TREND);
		if( !Check.isEmpty(defaultTrend) )
		{
			trend.setSelectedValue(info, Trend.valueOf(defaultTrend));
		}
		else
		{
			trend.setSelectedValue(info, Trend.WEEK);
		}
	}

	@Override
	protected void customSave(SectionInfo info, PortletEditingBean portlet)
	{
		portlet.setAttribute(KEY_DEFAULT_TREND, trend.getSelectedValueAsString(info));
	}

	@Override
	protected void customClear(SectionInfo info)
	{
		trend.setSelectedValue(info, Trend.WEEK);
	}

	@Override
	public Class<TaskStatisticsPortletEditorModel> getModelClass()
	{
		return TaskStatisticsPortletEditorModel.class;
	}

	public static class TaskStatisticsPortletEditorModel
		extends
			AbstractPortletEditorSection.AbstractPortletEditorModel
	{
		// Here there be dragons...
	}

	@Override
	public SectionRenderable renderHelp(RenderContext context)
	{
		return null;
	}

	public SingleSelectionList<Trend> getTrend()
	{
		return trend;
	}
}
