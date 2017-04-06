package com.tle.web.activation.viewitem.sidebar.summary;

import com.tle.beans.workflow.WorkflowStatus;
import com.tle.core.guice.Bind;
import com.tle.web.activation.viewitem.summary.section.ShowActivationsSection;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.viewitem.summary.sidebar.actions.GenericMinorActionWithPageSection;
import com.tle.web.viewurl.ItemSectionInfo;

/**
 * @author Aaron
 */
@Bind
public class ActivationsLinkSection extends GenericMinorActionWithPageSection
{
	@PlugKey("viewitem.sidebar.summary.activations.title")
	private static Label LABEL;

	@TreeLookup
	private ShowActivationsSection activationsContentSection;

	@Override
	protected Label getLinkLabel()
	{
		return LABEL;
	}

	@Override
	protected boolean canView(SectionInfo info, ItemSectionInfo itemInfo, WorkflowStatus status)
	{
		if( !itemInfo.getViewableItem().isItemForReal() )
		{
			return false;
		}
		return activationsContentSection.canView(info, itemInfo, status);
	}

	@Override
	protected SectionId getPageSection()
	{
		return activationsContentSection;
	}

	@Override
	public String getLinkText()
	{
		return LABEL.getText();
	}
}
