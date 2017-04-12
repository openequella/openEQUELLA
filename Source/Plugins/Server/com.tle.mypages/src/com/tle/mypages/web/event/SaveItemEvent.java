package com.tle.mypages.web.event;

import com.tle.beans.item.ItemPack;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;

/*
 * @author aholland
 */
public class SaveItemEvent extends AbstractMyPagesEvent<SaveItemEventListener>
{
	private final ItemPack itemPack;
	private boolean commit;

	public SaveItemEvent(ItemPack itemPack, String sessionId)
	{
		super(sessionId);
		this.itemPack = itemPack;
		this.commit = true;
	}

	@Override
	public void fire(SectionId sectionId, SectionInfo info, SaveItemEventListener listener) throws Exception
	{
		listener.doSaveItemEvent(info, this);
	}

	@Override
	public Class<SaveItemEventListener> getListenerClass()
	{
		return SaveItemEventListener.class;
	}

	public boolean isCommit()
	{
		return commit;
	}

	public void setCommit(boolean commit)
	{
		this.commit = commit;
	}

	public ItemPack getItemPack()
	{
		return itemPack;
	}
}
