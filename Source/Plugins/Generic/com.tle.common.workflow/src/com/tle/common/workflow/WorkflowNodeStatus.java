/*
 * Created on Aug 25, 2005
 */
package com.tle.common.workflow;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.tle.beans.IdCloneable;
import com.tle.beans.item.ModerationStatus;
import com.tle.common.DoNotClone;
import com.tle.common.DoNotSimplify;
import com.tle.common.workflow.node.WorkflowNode;

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
	public static final char ARCHIVED = 'a';

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	@ManyToOne
	@JoinColumn(insertable = false, updatable = false, nullable = false)
	@XStreamOmitField
	@Index(name = "nodeStatusModStatusIndex")
	private ModerationStatus modStatus;
	@ManyToOne
	@JoinColumn(name = "wnode_id", nullable = false)
	@DoNotSimplify
	@DoNotClone
	@Index(name = "nodeStatusNodeIndex")
	private WorkflowNode node;
	private char status;
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "node_id", nullable = false)
	private Set<WorkflowMessage> comments;

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

	public Set<WorkflowMessage> getComments()
	{
		if( comments == null )
		{
			comments = new HashSet<WorkflowMessage>();
		}
		return comments;
	}

	public void setComments(Set<WorkflowMessage> comments)
	{
		this.comments = comments;
	}

	public void archive()
	{
		status = WorkflowNodeStatus.ARCHIVED;
	}
}
