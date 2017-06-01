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

package com.tle.mypages.web.model;

import java.util.List;

import com.tle.web.viewitem.attachments.AttachmentView;

/**
 * @author aholland
 */
public class MyPagesSelectionModel
{
	private List<AttachmentView> attachments;
	private boolean selectMultiple;

	public List<AttachmentView> getAttachments()
	{
		return attachments;
	}

	public void setAttachments(List<AttachmentView> attachments)
	{
		this.attachments = attachments;
	}

	public boolean isSelectMultiple()
	{
		return selectMultiple;
	}

	public void setSelectMultiple(boolean selectMultiple)
	{
		this.selectMultiple = selectMultiple;
	}
}
