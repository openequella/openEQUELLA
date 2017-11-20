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

package com.tle.beans.item;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;

import org.hibernate.annotations.AccessType;

import com.tle.beans.IdCloneable;

@Entity
@AccessType("field")
public class HistoryEvent implements Serializable, IdCloneable
{
	private static final long serialVersionUID = 1L;

	public enum Type
	{
		statechange, resetworkflow, approved, rejected, edit, promoted, comment, assign, clone, changeCollection,
		newversion, contributed, workflowremoved, scriptComplete, scriptError, taskMove
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private String user;
	private Date date;

	@Column(length = 40)
	private String step;
	@Column(length = 40)
	private String toStep;
	@Column(length = 100)
	private String stepName;
	@Column(length = 100)
	private String toStepName;

	@Lob
	private String comment;
	private boolean applies;

	@Column(length = 25)
	private String type;
	private transient Type typeEnum;

	@Column(length = 25)
	private String state;
	private transient ItemStatus stateEnum;

	public HistoryEvent()
	{
		super();
	}

	public HistoryEvent(Item item)
	{
		setState(item.getStatus());
	}

	public HistoryEvent(Type type, Item item)
	{
		this(item);
		setType(type);
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

	public Date getDate()
	{
		return date;
	}

	public void setDate(Date date)
	{
		this.date = date;
	}

	public ItemStatus getState()
	{
		if( stateEnum == null && state != null )
		{
			stateEnum = ItemStatus.valueOf(state);
		}
		return stateEnum;
	}

	public void setState(ItemStatus state)
	{
		this.state = state.name();
		this.stateEnum = state;
	}

	public String getToStep()
	{
		return toStep;
	}

	public void setToStep(String tostep)
	{
		this.toStep = tostep;
	}

	public Type getType()
	{
		if( typeEnum == null && type != null )
		{
			typeEnum = Type.valueOf(type);
		}
		return typeEnum;
	}

	public void setType(Type type)
	{
		this.typeEnum = type;
		this.type = type.name();
	}

	public String getUser()
	{
		return user;
	}

	public void setUser(String user)
	{
		this.user = user;
	}

	public String getStep()
	{
		return step;
	}

	public void setStep(String step)
	{
		this.step = step;
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment(String comment)
	{
		this.comment = comment;
	}

	public boolean isApplies()
	{
		return applies;
	}

	public void setApplies(boolean applies)
	{
		this.applies = applies;
	}

	@Override
	public boolean equals(Object obj)
	{
		if( this == obj )
		{
			return true;
		}

		if( !(obj instanceof HistoryEvent) )
		{
			return false;
		}

		return id == ((HistoryEvent) obj).id;
	}

	@Override
	public int hashCode()
	{
		return (int) id;
	}

	public String getStepName()
	{
		return stepName;
	}

	public void setStepName(String stepName)
	{
		this.stepName = stepName;
	}

	public String getToStepName()
	{
		return toStepName;
	}

	public void setToStepName(String toStepName)
	{
		this.toStepName = toStepName;
	}
}
