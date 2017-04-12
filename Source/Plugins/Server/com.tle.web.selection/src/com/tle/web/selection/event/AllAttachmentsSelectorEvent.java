package com.tle.web.selection.event;

import com.tle.beans.item.ItemKey;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.AbstractSectionEvent;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.selection.SelectionSession;

public class AllAttachmentsSelectorEvent extends AbstractSectionEvent<AllAttachmentsSelectorEventListener>
{
	private final ItemKey itemId;
	private final SelectionSession session;
	private JSCallable function;

	public AllAttachmentsSelectorEvent(ItemKey itemId, SelectionSession session)
	{
		this.itemId = itemId;
		this.session = session;
	}

	@Override
	public Class<AllAttachmentsSelectorEventListener> getListenerClass()
	{
		return AllAttachmentsSelectorEventListener.class;
	}

	@Override
	public void fire(SectionId sectionId, SectionInfo info, AllAttachmentsSelectorEventListener listener)
	{
		listener.supplyFunction(info, this);
	}

	public ItemKey getItemId()
	{
		return itemId;
	}

	public SelectionSession getSession()
	{
		return session;
	}

	public JSCallable getFunction()
	{
		return function;
	}

	public void setFunction(JSCallable function)
	{
		this.function = function;
	}

}
