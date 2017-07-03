package com.tle.web.bulk.workflow.section;

import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;

@Bind
public class BulkRemoveWorkflowSection
	extends
		AbstractPrototypeSection<BulkRemoveWorkflowSection.BulkRemoveWorkflowModel>
	implements HtmlRenderer
{
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		getModel(context).setShowExecuteButton(true);
		return viewFactory.createResult("bulkremoveworkflow.ftl", context);
	}

	@Override
	public Class<BulkRemoveWorkflowModel> getModelClass()
	{
		return BulkRemoveWorkflowModel.class;
	}

	public static class BulkRemoveWorkflowModel
	{
		private String title;
		@Bookmarked
		private boolean showExecuteButton;

		public boolean isShowExecuteButton()
		{
			return showExecuteButton;
		}

		public void setShowExecuteButton(boolean showExecuteButton)
		{
			this.showExecuteButton = showExecuteButton;
		}

		public String getTitle()
		{
			return title;
		}

		public void setTitle(String title)
		{
			this.title = title;
		}
	}
}
