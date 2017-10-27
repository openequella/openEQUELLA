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

package com.tle.common.workflow.node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
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
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.tle.beans.IdCloneable;
import com.tle.beans.entity.LanguageBundle;
import com.tle.common.DoNotSimplify;
import com.tle.common.workflow.Workflow;

@Entity
@AccessType("field")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.CHAR)
@DiscriminatorValue("n")
public abstract class WorkflowNode implements IdCloneable, Serializable
{
	private static final long serialVersionUID = 1;

	public static final char SERIAL_TYPE = 's';
	public static final char PARALLEL_TYPE = 'p';
	public static final char DECISION_TYPE = 'd';
	public static final char ITEM_TYPE = 't';
	public static final char SCRIPT_TYPE ='x';

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(length = 40, nullable = false)
	private String uuid;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@Index(name = "workflowNodeName")
	protected LanguageBundle name;
	private boolean rejectPoint;

	@XStreamOmitField
	@ManyToOne
	@JoinColumn(name = "workflow_id", insertable = false, updatable = false, nullable = false)
	@Index(name = "workflowNodeWorkflow")
	protected Workflow workflow;

	@ManyToOne
	@DoNotSimplify
	@Index(name = "workflowNodeParent")
	protected WorkflowNode parent;
	private int childIndex;

	@Transient
	@XStreamOmitField
	private transient List<WorkflowNode> children;

	public WorkflowNode()
	{
	}

	public void setChildren(List<WorkflowNode> children)
	{
		this.children = children;
	}

	public WorkflowNode(LanguageBundle name)
	{
		this();
		this.name = name;
		this.uuid = UUID.randomUUID().toString();
	}

	public abstract char getType();

	public void setParent(WorkflowNode parent)
	{
		this.parent = parent;
	}

	public WorkflowNode getParent()
	{
		return parent;
	}

	public void setName(LanguageBundle name)
	{
		this.name = name;
	}

	public LanguageBundle getName()
	{
		return name;
	}

	public boolean canAddChildren()
	{
		return false;
	}

	public boolean isLeafNode()
	{
		return true;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if( this == obj )
		{
			return true;
		}
		if( obj == null )
		{
			return false;
		}
		if( getClass() != obj.getClass() )
		{
			return false;
		}
		final WorkflowNode other = (WorkflowNode) obj;
		if( uuid == null )
		{
			if( other.uuid != null )
			{
				return false;
			}
		}
		else if( !uuid.equals(other.uuid) )
		{
			return false;
		}
		return true;
	}

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
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

	public boolean isRejectPoint()
	{
		return rejectPoint;
	}

	public void setRejectPoint(boolean rejectPoint)
	{
		this.rejectPoint = rejectPoint;
	}

	public abstract boolean canHaveSiblingRejectPoints();

	public Workflow getWorkflow()
	{
		return workflow;
	}

	public void setWorkflow(Workflow workflow)
	{
		this.workflow = workflow;
	}

	public int getChildIndex()
	{
		return childIndex;
	}

	public WorkflowNode getChild(int index)
	{
		return getChildren().get(index);
	}

	public void addChild(WorkflowNode child)
	{
		List<WorkflowNode> childrenList = getChildren();
		child.childIndex = childrenList.size();
		childrenList.add(child);
	}

	public void addChild(int index, WorkflowNode child)
	{
		List<WorkflowNode> childrenList = getChildren();
		childrenList.add(index, child);
		while( index < childrenList.size() )
		{
			childrenList.get(index).childIndex = index;
			index++;
		}

	}

	public void removeChild(WorkflowNode child)
	{
		int index = children.indexOf(child);
		if( index >= 0 )
		{
			children.remove(child);
			while( index < children.size() )
			{
				children.get(index).childIndex = index;
				index++;
			}
		}
	}

	public int indexOfChild(WorkflowNode child)
	{
		return getChildren().indexOf(child);
	}

	public Iterator<WorkflowNode> iterateChildren()
	{
		return getChildren().iterator();
	}

	public int numberOfChildren()
	{
		return getChildren().size();
	}

	public void setChild(WorkflowNode node)
	{
		List<WorkflowNode> childrenList = getChildren();
		while( childrenList.size() <= node.childIndex )
		{
			childrenList.add(null);
		}
		childrenList.set(node.childIndex, node);
	}

	public void setChildIndex(int childIndex)
	{
		this.childIndex = childIndex;
	}

	public List<WorkflowNode> getChildren()
	{
		if( children == null )
		{
			children = new ArrayList<WorkflowNode>();
		}
		return children;
	}

}
