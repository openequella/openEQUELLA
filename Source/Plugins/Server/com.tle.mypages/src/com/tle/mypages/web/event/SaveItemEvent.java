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
