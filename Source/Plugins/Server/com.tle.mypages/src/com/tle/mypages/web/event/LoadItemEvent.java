package com.tle.mypages.web.event;

import com.tle.beans.item.Item;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;

/**
 * @author aholland
 */
public class LoadItemEvent extends AbstractMyPagesEvent<LoadItemEventListener>
{
	private final Item item;
	private final String pageUuid;

	public LoadItemEvent(String sessionId, Item item, String pageUuid)
	{
		super(sessionId);
		this.item = item;
		this.pageUuid = pageUuid;
	}

	@Override
	public void fire(SectionId sectionId, SectionInfo info, LoadItemEventListener listener) throws Exception
	{
		listener.doLoadItemEvent(info, this);
	}

	@Override
	public Class<LoadItemEventListener> getListenerClass()
	{
		return LoadItemEventListener.class;
	}

	public Item getItem()
	{
		return item;
	}

	public String getPageUuid()
	{
		return pageUuid;
	}
}
