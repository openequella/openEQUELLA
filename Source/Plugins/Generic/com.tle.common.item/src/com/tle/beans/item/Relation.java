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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.tle.beans.IdCloneable;

@Entity
@AccessType("field")
public class Relation implements Serializable, IdCloneable
{
	private static final long serialVersionUID = 4319349282045587399L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@JoinColumn(nullable = false)
	@ManyToOne
	@Index(name = "relationFirstItem")
	private Item firstItem;

	@JoinColumn(nullable = false)
	@ManyToOne
	@Index(name = "relationSecondItem")
	private Item secondItem;

	@Column(length = 255)
	private String firstResource;

	@Column(length = 255)
	private String secondResource;

	@Column(length = 40)
	private String relationType;

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

	public Item getFirstItem()
	{
		return firstItem;
	}

	public void setFirstItem(Item firstItem)
	{
		this.firstItem = firstItem;
	}

	public Item getSecondItem()
	{
		return secondItem;
	}

	public void setSecondItem(Item secondItem)
	{
		this.secondItem = secondItem;
	}

	public String getFirstResource()
	{
		return firstResource;
	}

	public void setFirstResource(String firstResource)
	{
		this.firstResource = firstResource;
	}

	public String getSecondResource()
	{
		return secondResource;
	}

	public void setSecondResource(String secondResource)
	{
		this.secondResource = secondResource;
	}

	public String getRelationType()
	{
		return relationType;
	}

	public void setRelationType(String relationType)
	{
		this.relationType = relationType;
	}
}
