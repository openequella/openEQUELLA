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

package com.tle.web.workflow.notification;

import com.tle.web.sections.Bookmark;
import com.tle.web.sections.render.Label;

public class ItemNotification
{
	private Label itemName;
	private Bookmark link;

	public Label getItemName()
	{
		return itemName;
	}

	public void setItemName(Label itemName)
	{
		this.itemName = itemName;
	}

	public Bookmark getLink()
	{
		return link;
	}

	public void setLink(Bookmark link)
	{
		this.link = link;
	}

}