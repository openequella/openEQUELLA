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

package com.tle.beans.user;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.IndexColumn;

import com.tle.beans.Institution;
import com.tle.common.Check;
import com.tle.common.Check.FieldEquality;
import com.tle.common.institution.TreeNodeInterface;

@Entity
@AccessType("field")
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"uuid", "institution_id"})})
public class TLEGroup implements TreeNodeInterface<TLEGroup>, FieldEquality<TLEGroup>
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(length = 40, nullable = false)
	@Index(name = "groupUuidIndex")
	private String uuid;

	@Column(length = 100)
	private String name;

	@Lob
	private String description;

	@ManyToOne
	@Index(name = "parentGroup")
	private TLEGroup parent;

	@ManyToMany
	@IndexColumn(name = "list_position")
	private List<TLEGroup> allParents = new ArrayList<TLEGroup>();

	@JoinColumn(nullable = false)
	@Index(name = "groupInstitutionIndex")
	@ManyToOne(fetch = FetchType.LAZY)
	private Institution institution;

	@ElementCollection(fetch = FetchType.LAZY)
	@Fetch(value = FetchMode.SUBSELECT)
	@Column(name = "element")
	private Set<String> users;

	public TLEGroup()
	{
		super();
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
	public TLEGroup getParent()
	{
		return parent;
	}

	@Override
	public void setParent(TLEGroup parent)
	{
		this.parent = parent;
	}

	@Override
	public void setAllParents(List<TLEGroup> allParents)
	{
		this.allParents = allParents;
	}

	@Override
	public List<TLEGroup> getAllParents()
	{
		return allParents;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
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

	public Set<String> getUsers()
	{
		if( users == null )
		{
			users = new HashSet<String>();
		}
		return users;
	}

	public void setUsers(Set<String> users)
	{
		this.users = users;
	}

	@Override
	public int hashCode()
	{
		return Check.getHashCode(id);
	}

	@Override
	public boolean equals(Object obj)
	{
		return Check.commonEquals(this, obj);
	}

	@Override
	public boolean checkFields(TLEGroup rhs)
	{
		return Objects.equals(id, rhs.id);
	}
}
