/*
 * Created on Aug 25, 2005
 */
package com.tle.core.workflow.migrate.beans;

import java.io.Serializable;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.AccessType;

import com.tle.beans.IdCloneable;
import com.tle.core.workflow.migrate.ModerationStatus;
import com.tle.core.workflow.migrate.beans.node.WorkflowNode;

@Entity
@AccessType("field")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING, name = "acttype")
@DiscriminatorValue("node")
public class WorkflowNodeStatus implements Serializable, IdCloneable
{
	private static final long serialVersionUID = 1L;

	public static final char INCOMPLETE = 'i';
	public static final char COMPLETE = 'c';

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	@ManyToOne
	private ModerationStatus modStatus;
	@ManyToOne
	@JoinColumn(name = "wnode_id")
	private WorkflowNode node;
	private char status;

	// To delete
	public String nodeId;
	public int type;

	public WorkflowNodeStatus()
	{
		super();
	}

	public WorkflowNodeStatus(WorkflowNode node)
	{
		this.node = node;
	}

	public char getStatus()
	{
		return status;
	}

	public void setStatus(char status)
	{
		this.status = status;
	}

	@Override
	public long getId()
	{
		return id;
	}

	@Override
	public void setId(long id)
	{
		this.id = id;
	}

	public WorkflowNode getNode()
	{
		return node;
	}

	public void setNode(WorkflowNode node)
	{
		this.node = node;
	}

	public ModerationStatus getModStatus()
	{
		return modStatus;
	}

	public void setModStatus(ModerationStatus modStatus)
	{
		this.modStatus = modStatus;
	}
}
