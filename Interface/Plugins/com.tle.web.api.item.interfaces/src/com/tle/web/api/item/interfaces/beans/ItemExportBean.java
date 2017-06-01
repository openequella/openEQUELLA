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

package com.tle.web.api.item.interfaces.beans;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.tle.web.api.interfaces.beans.AbstractExtendableBean;

public class ItemExportBean extends AbstractExtendableBean
{
	@SuppressWarnings("nls")
	private String exportVersion = "1.0";
	private List<HistoryEventBean> history;
	private List<ItemNodeStatusExportBean> statuses = new ArrayList<>();
	private boolean moderating;
	private Date liveApprovalDate;
	private Date reviewDate;
	private Date start;
	private Date lastAction;
	private String resumeStatus;
	private String deletedStatus;
	private String rejectedMessage;
	private String rejectedBy;
	private String rejectedStep;
	@JsonInclude(Include.NON_DEFAULT)
	private boolean resumeModerating;
	@JsonInclude(Include.NON_DEFAULT)
	private boolean unarchiveModerating;
	@JsonInclude(Include.NON_DEFAULT)
	private boolean deletedModerating;
	@JsonInclude(Include.NON_DEFAULT)
	private boolean needsReset;
	private ItemLockBean lock;

	public void setHistory(List<HistoryEventBean> history)
	{
		this.history = history;
	}

	public List<HistoryEventBean> getHistory()
	{
		return history;
	}

	public List<ItemNodeStatusExportBean> getStatuses()
	{
		return statuses;
	}

	public void setStatuses(List<ItemNodeStatusExportBean> statuses)
	{
		this.statuses = statuses;
	}

	public String getExportVersion()
	{
		return exportVersion;
	}

	public void setExportVersion(String exportVersion)
	{
		this.exportVersion = exportVersion;
	}

	public boolean isModerating()
	{
		return moderating;
	}

	public void setModerating(boolean moderating)
	{
		this.moderating = moderating;
	}

	public Date getLiveApprovalDate()
	{
		return liveApprovalDate;
	}

	public void setLiveApprovalDate(Date liveApprovalDate)
	{
		this.liveApprovalDate = liveApprovalDate;
	}

	public Date getReviewDate()
	{
		return reviewDate;
	}

	public void setReviewDate(Date reviewDate)
	{
		this.reviewDate = reviewDate;
	}

	public Date getStart()
	{
		return start;
	}

	public void setStart(Date start)
	{
		this.start = start;
	}

	public Date getLastAction()
	{
		return lastAction;
	}

	public void setLastAction(Date lastAction)
	{
		this.lastAction = lastAction;
	}

	public String getResumeStatus()
	{
		return resumeStatus;
	}

	public void setResumeStatus(String resumeStatus)
	{
		this.resumeStatus = resumeStatus;
	}

	public String getDeletedStatus()
	{
		return deletedStatus;
	}

	public void setDeletedStatus(String deletedStatus)
	{
		this.deletedStatus = deletedStatus;
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

	public boolean isResumeModerating()
	{
		return resumeModerating;
	}

	public void setResumeModerating(boolean resumeModerating)
	{
		this.resumeModerating = resumeModerating;
	}

	public boolean isUnarchiveModerating()
	{
		return unarchiveModerating;
	}

	public void setUnarchiveModerating(boolean unarchiveModerating)
	{
		this.unarchiveModerating = unarchiveModerating;
	}

	public boolean isDeletedModerating()
	{
		return deletedModerating;
	}

	public void setDeletedModerating(boolean deletedModerating)
	{
		this.deletedModerating = deletedModerating;
	}

	public boolean isNeedsReset()
	{
		return needsReset;
	}

	public void setNeedsReset(boolean needsReset)
	{
		this.needsReset = needsReset;
	}

	public ItemLockBean getLock()
	{
		return lock;
	}

	public void setLock(ItemLockBean lock)
	{
		this.lock = lock;
	}

}