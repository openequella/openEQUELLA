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

package com.tle.web.viewitem.summary.section;

import com.tle.web.sections.equella.component.model.SelectionsTableState;

public class NotifyModel
{
	/* private List<UserBean> users; */
	private SelectionsTableState selectedStuff;
	private boolean editable;

	// public List<UserBean> getUsers()
	// {
	// return users;
	// }
	//
	// public void setUsers(List<UserBean> users)
	// {
	// this.users = users;
	// }
	public SelectionsTableState getSelectedStuff()
	{
		return selectedStuff;
	}

	public void setSelectedStuff(SelectionsTableState selectedStuff)
	{
		this.selectedStuff = selectedStuff;
	}

	public boolean isEditable()
	{
		return editable;
	}

	public void setEditable(boolean editable)
	{
		this.editable = editable;
	}
}
