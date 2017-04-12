package com.tle.beans.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.tle.beans.item.ItemStatus;
import com.tle.core.workflow.events.WorkflowEvent;

/**
 * Represents the current status of an item in workflow.
 */
public class WorkflowStatus implements Serializable
{
	private static final long serialVersionUID = 1;

	private boolean moderating;
	private boolean rejected;
	private boolean moderationAllowed;

	private ItemStatus statusName;
	private List<WorkflowStep> currentSteps = new ArrayList<WorkflowStep>();
	private WorkflowEvent[] events;
	private Map<String, WorkflowStep> referencedSteps;
	private SecurityStatus securityStatus;

	private String ownerId;
	private boolean owner;

	public WorkflowStatus()
	{
		super();
	}

	public boolean isRejected()
	{
		return rejected;
	}

	public void setRejected(boolean rejected)
	{
		this.rejected = rejected;
	}

	public boolean isLocked()
	{
		return securityStatus.isLocked();
	}

	public boolean isModerating()
	{
		return moderating;
	}

	public void setModerating(boolean moderating)
	{
		this.moderating = moderating;
	}

	public boolean isOwner()
	{
		return owner;
	}

	public void setOwner(boolean owner)
	{
		this.owner = owner;
	}

	public WorkflowEvent[] getEvents()
	{
		return events;
	}

	public void setEvents(WorkflowEvent[] events)
	{
		this.events = events;
	}

	public String getOwnerId()
	{
		return ownerId;
	}

	public void setOwnerId(String owner)
	{
		this.ownerId = owner;
	}

	public boolean isArchived()
	{
		return getStatusName().equals(ItemStatus.ARCHIVED);
	}

	public ItemStatus getStatusName()
	{
		return statusName;
	}

	public void setStatusName(ItemStatus name)
	{
		statusName = name;
	}

	public Map<String, WorkflowStep> getReferencedSteps()
	{
		return referencedSteps;
	}

	public void setReferencedSteps(Map<String, WorkflowStep> referencedSteps)
	{
		this.referencedSteps = referencedSteps;
	}

	public List<WorkflowStep> getCurrentSteps()
	{
		return currentSteps;
	}

	public void setCurrentSteps(List<WorkflowStep> currentSteps)
	{
		this.currentSteps = currentSteps;
	}

	public void addCurrentStep(WorkflowStep step)
	{
		currentSteps.add(step);
	}

	public WorkflowStep getStepForId(String taskId)
	{
		if( taskId != null )
		{
			for( WorkflowStep step : currentSteps )
			{
				if( step.getUuid().equals(taskId) )
				{
					return step;
				}
			}
		}
		return null;
	}

	public SecurityStatus getSecurityStatus()
	{
		return securityStatus;
	}

	public void setSecurityStatus(SecurityStatus securityStatus)
	{
		this.securityStatus = securityStatus;
	}

	public boolean isModerationAllowed()
	{
		return moderationAllowed;
	}

	public void setModerationAllowed(boolean moderationAllowed)
	{
		this.moderationAllowed = moderationAllowed;
	}
}
