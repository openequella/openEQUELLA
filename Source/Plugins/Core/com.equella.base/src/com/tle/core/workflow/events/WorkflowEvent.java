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

package com.tle.core.workflow.events;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import com.tle.beans.item.HistoryEvent;
import com.tle.beans.item.HistoryEvent.Type;
import com.tle.beans.item.Item;

public abstract class WorkflowEvent implements Serializable
{
	private static final long serialVersionUID = 1L;
	protected HistoryEvent event;
	protected String userid;
	protected Date date;
	protected String stepName;
	protected String step;
	protected String tostep;
	protected String toStepName;

	public WorkflowEvent()
	{
		super();
	}

	public WorkflowEvent(HistoryEvent event)
	{
		this.event = event;
		date = event.getDate();
		userid = event.getUser();
		tostep = event.getToStep();
		step = event.getStep();
		stepName = event.getStepName();
		toStepName = event.getToStepName();
	}

	public static WorkflowEvent getEvent(HistoryEvent hevent)
	{
		WorkflowEvent event;
		switch( hevent.getType() )
		{
			case statechange:
				event = new StateChangeEvent(hevent);
				break;
			case taskMove:
				event = new StateChangeEvent(hevent);
				break;
			case scriptComplete:
				event = new WorkflowScriptEvent(hevent);
				break;
			case scriptError:
				event = new WorkflowScriptEvent(hevent);
				break;
			case approved:
				event = new ApprovedEvent(hevent);
				break;
			case resetworkflow:
				event = new ResetEvent(hevent);
				break;
			case rejected:
				event = new RejectedEvent(hevent);
				break;
			case edit:
				event = new EditEvent(hevent);
				break;
			case promoted:
				event = new PromotionEvent(hevent);
				break;
			case comment:
				event = new CommentEvent(hevent);
				break;
			case assign:
				event = new AssignEvent(hevent);
				break;
			case clone:
				event = new CloneEvent(hevent);
				break;
			case changeCollection:
				event = new MoveEvent(hevent);
				break;
			case newversion:
				event = new NewVersionEvent(hevent);
				break;
			case contributed:
				event = new ContributedEvent(hevent);
				break;
			case workflowremoved:
				event = new RemovedFromWorkflowEvent(hevent);
				break;
			default:
				// should probably throw an exception really?
				event = null;
		}
		return event;
	}

	public static WorkflowEvent[] getAllEvents(Item xml)
	{
		Collection<WorkflowEvent> events = new ArrayList<WorkflowEvent>();

		for( HistoryEvent event : xml.getHistory() )
		{
			events.add(getEvent(event));
		}

		return events.toArray(new WorkflowEvent[events.size()]);
	}

	public final Type getIntType()
	{
		return event.getType();
	}

	public Date getDate()
	{
		return date;
	}

	public void setDate(Date date)
	{
		this.date = date;
	}

	public String getStep()
	{
		return step;
	}

	public void setStep(String step)
	{
		this.step = step;
	}

	public String getUserid()
	{
		return userid;
	}

	public void setUserid(String userid)
	{
		this.userid = userid;
	}

	public String getTostep()
	{
		return tostep;
	}

	public void setTostep(String tostep)
	{
		this.tostep = tostep;
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

	public String getComment()
	{
		return event.getComment();
	}

	public long getId()
	{
		return event.getId();
	}
}
