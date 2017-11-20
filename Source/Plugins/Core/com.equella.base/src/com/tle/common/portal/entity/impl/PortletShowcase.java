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

package com.tle.common.portal.entity.impl;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.hibernate.annotations.AccessType;

import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.common.portal.entity.Portlet;

/**
 * @author aholland
 */
@Entity
@AccessType("field")
public class PortletShowcase
{
	public static final String TYPE = "showcase"; //$NON-NLS-1$

	@Id
	private long id;

	@OneToOne
	private Portlet portlet;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private Item item;
	@ManyToOne(fetch = FetchType.LAZY)
	private Attachment attachment;

	public Item getItem()
	{
		return item;
	}

	public void setItem(Item item)
	{
		this.item = item;
	}

	public Attachment getAttachment()
	{
		return attachment;
	}

	public void setAttachment(Attachment attachment)
	{
		this.attachment = attachment;
	}

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public Portlet getPortlet()
	{
		return portlet;
	}

	public void setPortlet(Portlet portlet)
	{
		this.portlet = portlet;
	}
}
