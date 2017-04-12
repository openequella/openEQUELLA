package com.tle.web.cloneormove.section;

import com.tle.beans.item.Item;
import com.tle.beans.workflow.WorkflowStatus;
import com.tle.core.guice.Bind;
import com.tle.web.cloneormove.model.RootCloneOrMoveModel;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.viewitem.summary.sidebar.actions.GenericMinorActionWithPageSection;
import com.tle.web.viewurl.ItemSectionInfo;

@Bind
public class CloneItemSection extends GenericMinorActionWithPageSection
{
	@PlugKey("cloneonly.title")
	private static Label LABEL_CLONE;

	@TreeLookup
	private RootCloneOrMoveSection rootCloneOrMoveSection;

	@Override
	protected Label getLinkLabel()
	{
		return LABEL_CLONE;
	}

	@Override
	protected boolean canView(SectionInfo info, ItemSectionInfo itemInfo, WorkflowStatus status)
	{
		return itemInfo.hasPrivilege(CloneOrMoveSection.CLONE_ITEM);
	}

	@Override
	public void execute(SectionInfo info)
	{
		final Item item = getItemInfo(info).getItem();

		rootCloneOrMoveSection.setCurrentItemdef(info, item.getItemDefinition().getUuid());

		RootCloneOrMoveModel model = rootCloneOrMoveSection.getModel(info);
		model.setUuid(item.getUuid());
		model.setVersion(item.getVersion());
		model.setIsMove(false);

		super.execute(info);
	}

	@Override
	protected SectionId getPageSection()
	{
		return rootCloneOrMoveSection;
	}

	@Override
	public String getLinkText()
	{
		return LABEL_CLONE.getText();
	}
}
