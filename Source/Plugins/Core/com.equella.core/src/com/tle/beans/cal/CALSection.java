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

package com.tle.beans.cal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.tle.core.copyright.Section;

@Entity
@AccessType("field")
@Table(name = "cal_section")
public class CALSection implements Section
{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	@JoinColumn(nullable = false)
	@Index(name = "portionIndex")
	@ManyToOne(fetch = FetchType.LAZY)
	private CALPortion portion;
	private String range;
	private String copyrightStatus;
	@Column(length = 40)
	@Index(name = "attachmentIndex")
	private String attachment;
	private Boolean illustration;

	@Override
	public boolean isIllustration()
	{
		return (illustration != null ? illustration : false);
	}

	public void setIllustration(boolean illustration)
	{
		this.illustration = illustration;
	}

	@Override
	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	@Override
	public String getRange()
	{
		return range;
	}

	public void setRange(String range)
	{
		this.range = range;
	}

	@Override
	public String getCopyrightStatus()
	{
		return copyrightStatus;
	}

	public void setCopyrightStatus(String copyrightStatus)
	{
		this.copyrightStatus = copyrightStatus;
	}

	@Override
	public String getAttachment()
	{
		return attachment;
	}

	public void setAttachment(String attachment)
	{
		if( attachment != null && attachment.length() >= 40 )
		{
			attachment = attachment.substring(0, 40);
		}
		this.attachment = attachment;
	}

	@Override
	public CALPortion getPortion()
	{
		return portion;
	}

	public void setPortion(CALPortion portion)
	{
		this.portion = portion;
	}

}
