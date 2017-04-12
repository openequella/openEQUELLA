package com.tle.mypages.web.event;

import com.tle.beans.item.attachments.HtmlAttachment;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;

/*
 * @author aholland
 */
public class SavePageEvent extends AbstractMyPagesEvent<SavePageEventListener>
{
	private final HtmlAttachment page;
	private boolean commit;

	public SavePageEvent(HtmlAttachment page, String sessionId)
	{
		super(sessionId);
		this.page = page;
		this.commit = true;
	}

	@Override
	public void fire(SectionId sectionId, SectionInfo info, SavePageEventListener listener) throws Exception
	{
		listener.doSavePageEvent(info, this);
	}

	@Override
	public Class<SavePageEventListener> getListenerClass()
	{
		return SavePageEventListener.class;
	}

	public HtmlAttachment getPage()
	{
		return page;
	}

	public boolean isCommit()
	{
		return commit;
	}

	public void setCommit(boolean commit)
	{
		this.commit = commit;
	}
}
