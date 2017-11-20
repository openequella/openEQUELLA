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
