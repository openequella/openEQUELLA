/*
 * Created on Aug 17, 2005
 */
package com.tle.common.workflow.node;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;

import org.hibernate.annotations.AccessType;

import com.tle.beans.entity.LanguageBundle;

@Entity(name = "WorkflowDecision")
@AccessType("field")
@DiscriminatorValue("d")
public class DecisionNode extends WorkflowTreeNode
{
	private static final long serialVersionUID = 1;

	@Lob
	private String script;
	@Column(length = 40)
	private String collectionUuid;

	public DecisionNode(LanguageBundle name)
	{
		super(name);
		script = ""; //$NON-NLS-1$
	}

	public DecisionNode()
	{
		super();
	}

	@Override
	public char getType()
	{
		return 'd';
	}

	public String getScript()
	{
		return script;
	}

	public void setScript(String script)
	{
		this.script = script;
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

	public String getCollectionUuid()
	{
		return collectionUuid;
	}

	public void setCollectionUuid(String collectionUuid)
	{
		this.collectionUuid = collectionUuid;
	}
}
