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

package com.tle.web.itemadmin;

import com.tle.beans.entity.BaseEntityLabel;
import com.tle.web.sections.render.Label;

public class WithinEntry
{
	private BaseEntityLabel bel;
	private Label group;
	private String typeId;
	private boolean where;
	private int order;
	private Label overrideLabel;
	private boolean simpleOpsOnly;

	public WithinEntry(BaseEntityLabel bel, Label group, String typeId, boolean where, int order)
	{
		super();
		this.bel = bel;
		this.group = group;
		this.typeId = typeId;
		this.where = where;
		this.order = order;
	}

	public BaseEntityLabel getBel()
	{
		return bel;
	}

	public void setBel(BaseEntityLabel bel)
	{
		this.bel = bel;
	}

	public Label getGroup()
	{
		return group;
	}

	public void setGroup(Label group)
	{
		this.group = group;
	}

	public String getTypeId()
	{
		return typeId;
	}

	public void setTypeId(String typeId)
	{
		this.typeId = typeId;
	}

	public boolean isWhere()
	{
		return where;
	}

	public void setWhere(boolean where)
	{
		this.where = where;
	}

	public int getOrder()
	{
		return order;
	}

	public void setOrder(int order)
	{
		this.order = order;
	}

	public Label getOverrideLabel()
	{
		return overrideLabel;
	}

	public void setOverrideLabel(Label overrideLabel)
	{
		this.overrideLabel = overrideLabel;
	}

	// http://dev.equella.com/issues/7658
	// If true will stop a bulk operation before it tries to ask for params
	public boolean isSimpleOpsOnly()
	{
		return simpleOpsOnly;
	}

	public void setSimpleOpsOnly(boolean simpleOpsOnly)
	{
		this.simpleOpsOnly = simpleOpsOnly;
	}
}
