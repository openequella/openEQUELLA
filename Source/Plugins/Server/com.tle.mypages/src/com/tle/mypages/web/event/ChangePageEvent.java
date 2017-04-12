package com.tle.mypages.web.event;

import java.util.EventListener;

import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;

/**
 * @author Aaron
 */
public class ChangePageEvent extends AbstractMyPagesEvent<ChangePageEventListener>
{
	private final String oldPageUuid;
	private final String newPageUuid;

	public ChangePageEvent(String oldPageUuid, String newPageUuid, String sessionId)
	{
		super(sessionId);
		this.oldPageUuid = oldPageUuid;
		this.newPageUuid = newPageUuid;
	}

	@Override
	public void fire(SectionId sectionId, SectionInfo info, ChangePageEventListener listener) throws Exception
	{
		listener.changePage(info, this);
	}

	@Override
	public Class<? extends EventListener> getListenerClass()
	{
		return ChangePageEventListener.class;
	}

	public String getOldPageUuid()
	{
		return oldPageUuid;
	}

	public String getNewPageUuid()
	{
		return newPageUuid;
	}
}
