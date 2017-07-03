/*
 * Created on Aug 17, 2005
 */
package com.tle.core.workflow.migrate.beans.node;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;

import org.hibernate.annotations.AccessType;

import com.tle.beans.entity.LanguageBundle;

@Entity(name = "WorkflowDecision")
@AccessType("field")
@DiscriminatorValue("d")
public class FakeDecisionNode extends FakeWorkflowNode
{
	private static final long serialVersionUID = 1;

	@Lob
	private String script;
	@Column(length = 40)
	private String collectionUuid;

	public FakeDecisionNode(LanguageBundle name)
	{
		super(name);
		script = ""; //$NON-NLS-1$
	}

	public FakeDecisionNode()
	{
		this(null);
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
