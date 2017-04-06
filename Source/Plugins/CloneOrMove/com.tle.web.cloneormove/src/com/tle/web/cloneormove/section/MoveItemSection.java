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
public class MoveItemSection extends GenericMinorActionWithPageSection
{
	@PlugKey("moveonly.title")
	private static Label MOVE_TITLE_LABEL;

	@TreeLookup
	private RootCloneOrMoveSection rootCloneOrMoveSection;

	@Override
	protected Label getLinkLabel()
	{
		return MOVE_TITLE_LABEL;
	}

	@Override
	protected boolean canView(SectionInfo info, ItemSectionInfo itemInfo, WorkflowStatus status)
	{
		return itemInfo.hasPrivilege(CloneOrMoveSection.MOVE_ITEM) && !status.isLocked();
	}

	@Override
	public void execute(SectionInfo info)
	{
		final Item item = getItemInfo(info).getItem();

		rootCloneOrMoveSection.setCurrentItemdef(info, item.getItemDefinition().getUuid());

		RootCloneOrMoveModel model = rootCloneOrMoveSection.getModel(info);
		model.setUuid(item.getUuid());
		model.setVersion(item.getVersion());
		model.setIsMove(true);
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
		return MOVE_TITLE_LABEL.getText();
	}
}
