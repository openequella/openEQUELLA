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
public class CommentBean extends AbstractExtendableBean
{
	private String uuid;
	private int rating;
	private boolean anonymous;
	private String comment;
	private UserBean postedBy;
	private Date postedDate;

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public int getRating()
	{
		return rating;
	}

	public void setRating(int rating)
	{
		this.rating = rating;
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment(String comment)
	{
		this.comment = comment;
	}

	public Date getPostedDate()
	{
		return postedDate;
	}

	public void setPostedDate(Date postedDate)
	{
		this.postedDate = postedDate;
	}

	public boolean isAnonymous()
	{
		return anonymous;
	}

	public void setAnonymous(boolean anonymous)
	{
		this.anonymous = anonymous;
	}

	public UserBean getPostedBy()
	{
		return postedBy;
	}

	public void setPostedBy(UserBean postedBy)
	{
		this.postedBy = postedBy;
	}
}
