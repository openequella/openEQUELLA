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

package com.tle.web.recipientselector;

import java.util.ArrayList;
import java.util.List;

import com.tle.common.usermanagement.user.valuebean.GroupBean;
import com.tle.common.usermanagement.user.valuebean.RoleBean;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.HtmlListState;
import com.tle.web.sections.standard.model.Option;

public class SelectionDisplayTreeNode
{
	private String id;
	private HtmlListState grouping;
	private HtmlLinkState delete;
	private List<ExpressionSelection> expression;
	private List<SelectionDisplayTreeNode> children = new ArrayList<SelectionDisplayTreeNode>();

	public HtmlListState getGrouping()
	{
		return grouping;
	}


	public void setGrouping(HtmlListState grouping)
	{
		this.grouping = grouping;
	}


	public HtmlLinkState getDelete()
	{
		return delete;
	}


	public void setDelete(HtmlLinkState delete)
	{
		this.delete = delete;
	}


	public List<ExpressionSelection> getExpression()
	{
		return expression;
	}


	public void setExpression(List<ExpressionSelection> expression)
	{
		this.expression = expression;
	}

	public void addChildren(SelectionDisplayTreeNode children)
	{
		this.children.add(children);
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public List<SelectionDisplayTreeNode> getChildren()
	{
		return children;
	}


	public void setChildren(List<SelectionDisplayTreeNode> children)
	{
		this.children = children;
	}

	public static class ExpressionSelection
	{
		private String selection;
		private HtmlLinkState deleteSelection;

		public ExpressionSelection(String selection)
		{
			this.selection = selection;
		}

		public String getSelection()
		{
			return selection;
		}

		public void setSelection(String selection)
		{
			this.selection = selection;
		}

		public HtmlLinkState getDeleteSelection()
		{
			return deleteSelection;
		}

		public void setDeleteSelection(HtmlLinkState deleteSelection)
		{
			this.deleteSelection = deleteSelection;
		}
	}

	public static class SelectUserResultOption implements Option<UserBean>
	{
		private final UserBean user;
		private final HtmlLinkState link;
		private final HtmlLinkState add;

		protected SelectUserResultOption(UserBean user, HtmlLinkState link, HtmlLinkState add)
		{
			this.user = user;
			this.link = link;
			this.add = add;
		}

		@Override
		public UserBean getObject()
		{
			return user;
		}

		public HtmlLinkState getLink()
		{
			return link;
		}

		public HtmlLinkState getAdd()
		{
			return add;
		}

		public Label getUsername()
		{
			return new TextLabel(user.getUsername());
		}

		@Override
		public String getName()
		{
			return null;
		}

		@Override
		public String getValue()
		{
			return user.getUniqueID();
		}

		@Override
		public String getAltTitleAttr()
		{
			return null;
		}

		@Override
		public String getGroupName()
		{
			return null;
		}

		@Override
		public boolean isDisabled()
		{
			return false;
		}

		@Override
		public boolean isNameHtml()
		{
			return false;
		}

		@Override
		public boolean hasAltTitleAttr()
		{
			return false;
		}

		@Override
		public void setDisabled(boolean disabled)
		{
			// nothing
		}
	}

	public static class SelectGroupResultOption implements Option<GroupBean>
	{
		private final GroupBean group;
		private final HtmlLinkState link;

		public SelectGroupResultOption(GroupBean group, HtmlLinkState link)
		{
			this.group = group;
			this.link = link;
		}

		@Override
		public GroupBean getObject()
		{
			return group;
		}

		@Override
		public String getName()
		{
			return group.getName();
		}

		@Override
		public String getValue()
		{
			return group.getUniqueID();
		}

		@Override
		public String getAltTitleAttr()
		{
			return null;
		}

		@Override
		public String getGroupName()
		{
			return group.getName();
		}

		@Override
		public boolean isDisabled()
		{
			return false;
		}

		@Override
		public boolean isNameHtml()
		{
			return false;
		}

		@Override
		public boolean hasAltTitleAttr()
		{
			return false;
		}

		public GroupBean getGroup()
		{
			return group;
		}

		public HtmlLinkState getLink()
		{
			return link;
		}

		@Override
		public void setDisabled(boolean disabled)
		{
			// nothing
		}
	}

	public static class SelectRoleResultOption implements Option<RoleBean>
	{
		private final RoleBean role;
		private final HtmlLinkState link;

		public SelectRoleResultOption(RoleBean role, HtmlLinkState link)
		{
			this.role = role;
			this.link = link;
		}

		public RoleBean getRole()
		{
			return role;
		}
		public HtmlLinkState getLink()
		{
			return link;
		}

		@Override
		public RoleBean getObject()
		{
			return role;
		}

		@Override
		public String getName()
		{
			return role.getName();
		}

		@Override
		public String getValue()
		{
			return role.getUniqueID();
		}

		@Override
		public String getAltTitleAttr()
		{
			return null;
		}

		@Override
		public String getGroupName()
		{
			return null;
		}

		@Override
		public boolean isDisabled()
		{
			return false;
		}

		@Override
		public boolean isNameHtml()
		{
			return false;
		}

		@Override
		public boolean hasAltTitleAttr()
		{
			return false;
		}

		@Override
		public void setDisabled(boolean disabled)
		{
			// nothing
		}
	}
}

