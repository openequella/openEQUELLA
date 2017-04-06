/*
 * Created on Aug 17, 2005
 */
package com.tle.common.workflow.node;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.hibernate.annotations.AccessType;

import com.tle.beans.entity.LanguageBundle;

@Entity(name = "WorkflowSerial")
@AccessType("field")
@DiscriminatorValue("s")
public class SerialNode extends WorkflowTreeNode
{
	private static final long serialVersionUID = 1;

	public SerialNode(LanguageBundle name)
	{
		super(name);
	}

	public SerialNode()
	{
		super();
	}

	@Override
	public char getType()
	{
		return 's';
	}

	@Override
	public boolean canHaveSiblingRejectPoints()
	{
		return true;
	}
}
