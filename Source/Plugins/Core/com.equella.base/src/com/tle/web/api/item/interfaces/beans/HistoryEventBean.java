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

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import com.tle.web.api.interfaces.beans.AbstractExtendableBean;
import com.tle.web.api.interfaces.beans.UserBean;


@XmlRootElement
public class HistoryEventBean extends AbstractExtendableBean
{
	private UserBean user;
	private Date date;
	private String step;
	private String toStep;
	private String stepName;
	private String toStepName;
	private String comment;
	// WTF is this?
	// private boolean applies;
	private String type;
	private String state;

	public UserBean getUser()
	{
		return user;
	}

	public void setUser(UserBean user)
	{
		this.user = user;
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

	public String getToStep()
	{
		return toStep;
	}

	public void setToStep(String toStep)
	{
		this.toStep = toStep;
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
		return comment;
	}

	public void setComment(String comment)
	{
		this.comment = comment;
	}

	// public boolean isApplies()
	// {
	// return applies;
	// }
	//
	// public void setApplies(boolean applies)
	// {
	// this.applies = applies;
	// }

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getState()
	{
		return state;
	}

	public void setState(String state)
	{
		this.state = state;
	}
}
