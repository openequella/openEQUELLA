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

import com.tle.beans.entity.LanguageBundle;

public class DecisionNode extends WorkflowTreeNode
{
	private static final long serialVersionUID = 1;

	private String script;
	private long scriptID;

	public DecisionNode(LanguageBundle name)
	{
		super(name);
		script = ""; //$NON-NLS-1$
	}

	public DecisionNode()
	{
		this(null);
	}

	@Override
	protected int getDefaultType()
	{
		return WorkflowNode.DECISION_TYPE;
	}

	public String getScript()
	{
		return script;
	}

	public void setScript(String script)
	{
		this.script = script;
	}

	public long getScriptID()
	{
		return scriptID;
	}

	public void setScriptID(long scriptID)
	{
		this.scriptID = scriptID;
	}

	@Override
	public boolean canAddChildren()
	{
		return true;
	}

	@Override
	public boolean canHaveSiblingRejectPoints()
	{
		return true;
	}
}
