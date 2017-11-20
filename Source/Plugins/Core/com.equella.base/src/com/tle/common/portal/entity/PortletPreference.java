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

package com.tle.common.portal.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

/**
 * @author aholland
 */
@Entity
@AccessType("field")
public class PortletPreference
{
	public static final int POSITION_TOP = 1;
	public static final int POSITION_LEFT = 2;
	public static final int POSITION_RIGHT = 3;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@JoinColumn(nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	@Index(name = "portletPrefPortletIndex")
	private Portlet portlet;

	@Index(name = "portletPrefUserIndex")
	@Column(length = 40, nullable = false)
	private String userId;

	private int position;
	private int order;
	private boolean closed;
	private boolean minimised;

	@SuppressWarnings("unused")
	private PortletPreference()
	{
		// hibernate
	}

	public PortletPreference(Portlet portlet, String userId)
	{
		this.portlet = portlet;
		this.userId = userId;
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

	public String getUserId()
	{
		return userId;
	}

	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	public int getPosition()
	{
		return position;
	}

	/**
	 * @param position One of PortletPreference.POSITION_TOP,
	 *            PortletPreference.POSITION_TOP or
	 *            PortletPreference.POSITION_TOP
	 */
	public void setPosition(int position)
	{
		this.position = position;
	}

	public int getOrder()
	{
		return order;
	}

	public void setOrder(int order)
	{
		this.order = order;
	}

	/**
	 * Only applicable to preferences on institution-wide portlets. User defined
	 * portlets are simply deleted when closed.
	 * 
	 * @return
	 */
	public boolean isClosed()
	{
		return closed;
	}

	public void setClosed(boolean closed)
	{
		this.closed = closed;
	}

	public boolean isMinimised()
	{
		return minimised;
	}

	public void setMinimised(boolean minimised)
	{
		this.minimised = minimised;
	}
}
