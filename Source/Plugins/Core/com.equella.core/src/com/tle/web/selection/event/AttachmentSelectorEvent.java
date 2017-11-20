/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.selection.event;

import com.tle.beans.item.ItemKey;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.AbstractSectionEvent;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.selection.SelectAttachmentHandler;
import com.tle.web.selection.SelectionSession;

/**
 * Asks the current SectionTree set to see if there is something that handles
 * attachment selection
 * 
 * @author Aaron
 */
public class AttachmentSelectorEvent extends AbstractSectionEvent<AttachmentSelectorEventListener>
{
	private final ItemKey itemId;
	private final SelectionSession session;
	private JSCallable function;
	/**
	 * Much the same as the function, only it's something invokable in Java
	 */
	private SelectAttachmentHandler handler;

	public AttachmentSelectorEvent(ItemKey itemId, SelectionSession session)
	{
		this.itemId = itemId;
		this.session = session;
	}

	@Override
	public Class<AttachmentSelectorEventListener> getListenerClass()
	{
		return AttachmentSelectorEventListener.class;
	}

	@Override
	public void fire(SectionId sectionId, SectionInfo info, AttachmentSelectorEventListener listener)
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

	/**
	 * The function you supply must conform to: public void
	 * selectAttachment(SectionInfo info, String uuid, ItemId itemId, String
	 * extensionType)
	 * 
	 * @param function
	 */
	public void setFunction(JSCallable function)
	{
		this.function = function;
	}

	/**
	 * @return Much the same as the function, only it's something invokable in
	 *         Java
	 */
	public SelectAttachmentHandler getHandler()
	{
		return handler;
	}

	public void setHandler(SelectAttachmentHandler handler)
	{
		this.handler = handler;
	}
}
