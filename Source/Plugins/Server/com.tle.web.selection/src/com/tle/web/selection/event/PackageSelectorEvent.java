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

import com.tle.beans.item.IItem;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.AbstractSectionEvent;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.selection.SelectionSession;

/**
 * Asks the current SectionTree set to see if there is something that handles
 * Package selection
 * 
 * @author Aaron
 */
public class PackageSelectorEvent extends AbstractSectionEvent<PackageSelectorEventListener>
{
	private final IItem<?> item;
	private final SelectionSession session;
	private JSCallable function;

	public PackageSelectorEvent(IItem<?> item, SelectionSession session)
	{
		this.item = item;
		this.session = session;
	}

	@Override
	public Class<PackageSelectorEventListener> getListenerClass()
	{
		return PackageSelectorEventListener.class;
	}

	@Override
	public void fire(SectionId sectionId, SectionInfo info, PackageSelectorEventListener listener)
	{
		listener.supplyFunction(info, this);
	}

	public IItem<?> getItem()
	{
		return item;
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
