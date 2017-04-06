package com.tle.web.wizard;

import java.io.Serializable;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.filesystem.FileHandle;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.attachments.ModifiableAttachments;
import com.tle.beans.workflow.WorkflowStatus;

public interface WizardStateInterface extends Serializable, Cloneable
{
	String getWizid();

	ModifiableAttachments getAttachments();

	void setItemPack(ItemPack<Item> pack);

	ItemPack<Item> getItemPack();

	FileHandle getFileHandle();

	Item getItem();

	ItemKey getItemId();

	PropBagEx getItemxml();

	WorkflowStatus getWorkflowStatus();

	String getStagingId();

	void onSessionSave();

	WizardStateInterface clone();
}
