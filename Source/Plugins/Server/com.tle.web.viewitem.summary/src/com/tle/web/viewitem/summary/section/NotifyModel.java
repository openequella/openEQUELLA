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
