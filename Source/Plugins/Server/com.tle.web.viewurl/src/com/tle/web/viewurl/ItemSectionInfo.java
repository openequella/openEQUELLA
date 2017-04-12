package com.tle.web.viewurl;

import java.util.Set;

import com.dytech.devlib.PropBagEx;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.attachments.Attachments;
import com.tle.beans.workflow.WorkflowStatus;
import com.tle.core.workflow.operations.WorkflowOperation;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.viewable.ViewableItem;

@NonNullByDefault
public interface ItemSectionInfo
{
	ItemKey getItemId();

	String getItemdir();

	Item getItem();

	PropBagEx getItemxml();

	ItemDefinition getItemdef();

	@Nullable
	WorkflowStatus getWorkflowStatus();

	Set<String> getPrivileges();

	boolean hasPrivilege(String privilege);

	Attachments getAttachments();

	void modify(WorkflowOperation... ops);

	void refreshItem(boolean modified);

	@Nullable
	ViewableItem<Item> getViewableItem();

	@TreeIndexed
	interface ItemSectionInfoFactory extends SectionId
	{
		ItemSectionInfo getItemSectionInfo(SectionInfo info);
	}

	boolean isEditing();
}
