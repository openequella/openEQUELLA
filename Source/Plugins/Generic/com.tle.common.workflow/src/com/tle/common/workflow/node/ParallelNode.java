/*
 * Created on Aug 17, 2005
 */
package com.tle.common.workflow.node;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.hibernate.annotations.AccessType;

import com.tle.beans.entity.LanguageBundle;

@Entity(name = "WorkflowParallel")
@AccessType("field")
@DiscriminatorValue("p")
public class ParallelNode extends WorkflowTreeNode
{
	private static final long serialVersionUID = 1;

	public ParallelNode(LanguageBundle name)
	{
		super(name);
	}

	public ParallelNode()
	{
		super();
	}

	@Override
	public char getType()
	{
		return 'p';
	}

	@Override
	public boolean canHaveSiblingRejectPoints()
	{
		return false;
	}
}
