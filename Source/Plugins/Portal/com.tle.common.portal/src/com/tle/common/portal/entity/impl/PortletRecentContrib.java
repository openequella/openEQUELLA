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

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.portal.entity.Portlet;

/**
 * @author aholland
 */
@Entity
@AccessType("field")
public class PortletRecentContrib implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	private long id;

	@OneToOne
	@JoinColumn(nullable = false)
	@Index(name = "portrc_portlet")
	private Portlet portlet;

	@Column(length = 255)
	private String userId;

	@ManyToMany(fetch = FetchType.EAGER)
	private List<ItemDefinition> collections;

	private int ageDays;

	@Column(length = 255)
	private String query;

	/**
	 * @return
	 */
	public String getUserId()
	{
		return userId;
	}

	/**
	 * @param userId Show items contributed by this user. If null, then all
	 *            users
	 */
	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	public List<ItemDefinition> getCollections()
	{
		return collections;
	}

	/**
	 * @param collections Show items contributed into these collections. If
	 *            empty/null then all collections
	 */
	public void setCollections(List<ItemDefinition> collections)
	{
		this.collections = collections;
	}

	public int getAgeDays()
	{
		return ageDays;
	}

	public void setAgeDays(int ageDays)
	{
		this.ageDays = ageDays;
	}

	public String getQuery()
	{
		return query;
	}

	public void setQuery(String query)
	{
		this.query = query;
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
