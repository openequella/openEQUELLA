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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToMany;

import org.hibernate.annotations.AccessType;

import com.tle.beans.IdCloneable;
import com.tle.common.workflow.WorkflowNodeStatus;

@Entity
@AccessType("field")
public class ModerationStatus implements Serializable, IdCloneable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private Date liveApprovalDate;
	private Date reviewDate;
	private Date start;
	private Date lastAction;
	private String resumeStatus2;
	private String deletedStatus2;
	private transient ItemStatus resumeStatusEnum;
	private transient ItemStatus deletedStatusEnum;
	private Boolean resumeModerating;
	private Boolean unarchiveModerating;
	private Boolean deletedModerating;
	private boolean needsReset;
	@Lob
	private String rejectedMessage;
	@Column(length = 255)
	private String rejectedBy;
	@Column(length = 40)
	private String rejectedStep;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "mod_status_id", nullable = false)
	private Set<WorkflowNodeStatus> statuses;

	public Date getStart()
	{
		if( start == null )
		{
			start = new Date();
		}
		return start;
	}

	public void setStart(Date start)
	{
		this.start = start;
	}

	public Set<WorkflowNodeStatus> getStatuses()
	{
		if( statuses == null )
		{
			statuses = new HashSet<WorkflowNodeStatus>();
		}
		return statuses;
	}

	public void setStatuses(Set<WorkflowNodeStatus> statuses)
	{
		this.statuses = statuses;
	}

	public Date getLiveApprovalDate()
	{
		return liveApprovalDate;
	}

	public void setLiveApprovalDate(Date liveApprovalDate)
	{
		this.liveApprovalDate = liveApprovalDate;
	}

	public void setReviewDate(Date reviewDate)
	{
		this.reviewDate = reviewDate;
	}

	public Date getReviewDate()
	{
		return reviewDate;
	}

	public ItemStatus getDeletedStatus()
	{
		if( deletedStatusEnum == null && deletedStatus2 != null )
		{
			deletedStatusEnum = ItemStatus.valueOf(deletedStatus2);
		}
		return deletedStatusEnum;
	}

	public void setDeletedStatus(ItemStatus deletedStatus)
	{
		this.deletedStatusEnum = deletedStatus;
		this.deletedStatus2 = deletedStatus != null ? deletedStatus.name() : null;
	}

	public ItemStatus getResumeStatus()
	{
		if( resumeStatusEnum == null && resumeStatus2 != null )
		{
			resumeStatusEnum = ItemStatus.valueOf(resumeStatus2);
		}
		return resumeStatusEnum;
	}

	public void setResumeStatus(ItemStatus resumeStatus)
	{
		this.resumeStatusEnum = resumeStatus;
		this.resumeStatus2 = resumeStatus != null ? resumeStatus.name() : null;
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

	public boolean isResumeModerating()
	{
		return resumeModerating == null ? false : resumeModerating;
	}

	public void setResumeModerating(boolean resumeModerating)
	{
		this.resumeModerating = resumeModerating;
	}

	public boolean isUnarchiveModerating()
	{
		return unarchiveModerating == null ? false : unarchiveModerating;
	}

	public void setUnarchiveModerating(boolean unarchiveModerating)
	{
		this.unarchiveModerating = unarchiveModerating;
	}

	public boolean isNeedsReset()
	{
		return needsReset;
	}

	public void setNeedsReset(boolean needsReset)
	{
		this.needsReset = needsReset;
	}

	public boolean isDeletedModerating()
	{
		return (deletedModerating != null && deletedModerating) || deletedStatusEnum == ItemStatus.MODERATING;
	}

	public void setDeletedModerating(boolean deletedModerating)
	{
		this.deletedModerating = deletedModerating;
	}

	public Date getLastAction()
	{
		return lastAction;
	}

	public void setLastAction(Date lastAction)
	{
		this.lastAction = lastAction;
	}

	public String getRejectedMessage()
	{
		return rejectedMessage;
	}

	public void setRejectedMessage(String rejectedMessage)
	{
		this.rejectedMessage = rejectedMessage;
	}

	public String getRejectedBy()
	{
		return rejectedBy;
	}

	public void setRejectedBy(String rejectedBy)
	{
		this.rejectedBy = rejectedBy;
	}

	public String getRejectedStep()
	{
		return rejectedStep;
	}

	public void setRejectedStep(String rejectedStep)
	{
		this.rejectedStep = rejectedStep;
	}
}
