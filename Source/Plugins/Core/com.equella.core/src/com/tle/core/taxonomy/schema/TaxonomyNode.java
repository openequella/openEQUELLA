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

package com.tle.core.taxonomy.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.Type;

import com.tle.beans.Institution;
import com.tle.common.Check;
import com.tle.common.Check.FieldEquality;
import com.tle.common.institution.TreeNodeInterface;

/**
 * @author Nicholas Read
 */
@Entity
@AccessType("field")
public class TaxonomyNode implements TreeNodeInterface<TaxonomyNode>, FieldEquality<TaxonomyNode>
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(nullable = false)
	private String uuid;

	@Column(nullable = false)
	@Type(type = "blankable")
	private String name;

	@JoinColumn(nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Institution institution;

	@ManyToOne(fetch = FetchType.LAZY)
	private TaxonomyNode parent;

	@ManyToMany(fetch = FetchType.LAZY)
	@IndexColumn(name = "list_position")
	private List<TaxonomyNode> allParents = new ArrayList<TaxonomyNode>();

	private String fullpath;

	public TaxonomyNode()
	{
		super();
	}

	public String getFullpath()
	{
		return fullpath;
	}

	public void setFullpath(String fullpath)
	{
		this.fullpath = fullpath;
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

	@Override
	public String getUuid()
	{
		return uuid;
	}

	@Override
	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public Institution getInstitution()
	{
		return institution;
	}

	@Override
	public void setInstitution(Institution institution)
	{
		this.institution = institution;
	}

	@Override
	public TaxonomyNode getParent()
	{
		return parent;
	}

	@Override
	public void setParent(TaxonomyNode parent)
	{
		this.parent = parent;
	}

	@Override
	public List<TaxonomyNode> getAllParents()
	{
		return allParents;
	}

	@Override
	public void setAllParents(List<TaxonomyNode> allParents)
	{
		this.allParents = allParents;
	}

	@Override
	public boolean equals(Object obj)
	{
		return Check.commonEquals(this, obj);
	}

	@Override
	public boolean checkFields(TaxonomyNode rhs)
	{
		return Objects.equals(id, rhs.id);
	}

	@Override
	public int hashCode()
	{
		return Long.valueOf(id).hashCode();
	}
}
