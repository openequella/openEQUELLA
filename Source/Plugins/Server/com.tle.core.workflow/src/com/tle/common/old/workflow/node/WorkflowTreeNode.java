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

package com.tle.common.old.workflow.node;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.tle.beans.entity.LanguageBundle;

public abstract class WorkflowTreeNode extends WorkflowNode
{
	private static final long serialVersionUID = 1;

	private List<WorkflowNode> children;
	private boolean rejectPoint;

	public WorkflowTreeNode(LanguageBundle name)
	{
		super(name);
		children = new ArrayList<WorkflowNode>();
	}

	public WorkflowTreeNode()
	{
		this(null);
	}

	public WorkflowNode getChild(int index)
	{
		return children.get(index);
	}

	public void addChild(WorkflowNode child)
	{
		children.add(child);
	}

	public void addChild(int index, WorkflowNode child)
	{
		children.add(index, child);
	}

	public void removeChild(WorkflowNode child)
	{
		children.remove(child);
	}

	public int indexOfChild(WorkflowNode child)
	{
		return children.indexOf(child);
	}

	public Iterator<WorkflowNode> iterateChildren()
	{
		return children.iterator();
	}

	public int numberOfChildren()
	{
		return children.size();
	}

	@Override
	public boolean canAddChildren()
	{
		return true;
	}

	@Override
	protected int getDefaultType()
	{
		return WorkflowNode.ROOT_TYPE;
	}

	@Override
	public boolean isLeafNode()
	{
		return false;
	}

	public boolean isRejectPoint()
	{
		return rejectPoint;
	}

	public void setRejectPoint(boolean rejectPoint)
	{
		this.rejectPoint = rejectPoint;
	}

	public abstract boolean canHaveSiblingRejectPoints();
}
