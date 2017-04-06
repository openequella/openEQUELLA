/*
 * Created on Aug 17, 2005
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
