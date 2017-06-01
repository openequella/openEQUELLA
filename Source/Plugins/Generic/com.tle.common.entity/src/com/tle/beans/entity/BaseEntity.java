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

package com.tle.beans.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

import com.tle.beans.Institution;
import com.tle.common.Check;
import com.tle.common.Check.FieldEquality;

/**
 * @author jmaginnis
 */
@Entity
@AccessType("field")
@Inheritance(strategy = InheritanceType.JOINED)
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"institution_id", "uuid"})})
public class BaseEntity implements Serializable, FieldEquality<BaseEntity>
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	@Column(length = 40, nullable = false)
	@Index(name = "uuidIndex")
	private String uuid;
	@JoinColumn(nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	@Index(name = "institutionIndex")
	private Institution institution;
	@Type(type = "blankable")
	private String owner;
	@Column(nullable = false)
	private Date dateModified;
	@Column(nullable = false)
	private Date dateCreated;
	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@Index(name = "baseEntityDescription")
	private LanguageBundle description;
	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@Index(name = "baseEntityName")
	private LanguageBundle name;

	@JoinColumn
	@ElementCollection(fetch = FetchType.LAZY)
	@Fetch(value = FetchMode.SUBSELECT)
	@JoinTable(name = "BaseEntity_attributes")
	private List<Attribute> attributes;

	// was once member of ItemDefinition
	@Index(name = "baseEntitySystemTypeIndex")
	private boolean systemType;

	@Column(nullable = false)
	@Index(name = "disabledIndex")
	private boolean disabled;

	public BaseEntity()
	{
		super();
	}

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public String getOwner()
	{
		return owner;
	}

	public void setOwner(String owner)
	{
		this.owner = owner;
	}

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String id)
	{
		this.uuid = id;
	}

	public Institution getInstitution()
	{
		return institution;
	}

	public void setInstitution(Institution institution)
	{
		this.institution = institution;
	}

	public Date getDateCreated()
	{
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated)
	{
		this.dateCreated = dateCreated;
	}

	public Date getDateModified()
	{
		return dateModified;
	}

	public void setDateModified(Date dateModified)
	{
		this.dateModified = dateModified;
	}

	public LanguageBundle getDescription()
	{
		return description;
	}

	public void setDescription(LanguageBundle description)
	{
		this.description = description;
	}

	public LanguageBundle getName()
	{
		return name;
	}

	public void setName(LanguageBundle name)
	{
		this.name = name;
	}

	public boolean isSystemType()
	{
		return systemType;
	}

	public void setSystemType(boolean systemType)
	{
		this.systemType = systemType;
	}

	public boolean isDisabled()
	{
		return disabled;
	}

	public void setDisabled(boolean disabled)
	{
		this.disabled = disabled;
	}

	// Sonar's objection concerning BaseEntity subclasses (Dodgy - Class doesn't
	// override equals in superclass) sufficiently attended to by commonEquals
	@Override
	public boolean equals(Object obj)
	{
		return Check.commonEquals(this, obj); // NOSONAR
	}

	@Override
	public boolean checkFields(BaseEntity rhs)
	{
		return id == rhs.getId();
	}

	@Override
	public int hashCode()
	{
		return Long.valueOf(id).hashCode();
	}

	public Map<String, String> getAttributes()
	{
		Map<String, String> results = new HashMap<String, String>();
		if( attributes != null )
		{
			for( Attribute attribute : attributes )
			{
				results.put(attribute.getKey(), attribute.getValue());
			}
		}
		return results;
	}

	public void setAttributes(Map<String, String> values)
	{
		attributes = null;
		if( values != null )
		{
			attributes = new ArrayList<Attribute>();
			for( Map.Entry<String, String> entry : values.entrySet() )
			{
				attributes.add(new Attribute(entry.getKey(), entry.getValue()));
			}
		}
	}

	public String getAttribute(String key)
	{
		if( attributes != null )
		{
			for( Attribute att : attributes )
			{
				if( att.getKey().equals(key) )
				{
					return att.getValue();
				}
			}
		}
		return null;
	}

	public boolean getAttribute(String key, boolean defaultValue)
	{
		String bool = getAttribute(key);
		if( Boolean.TRUE.toString().equalsIgnoreCase(bool) )
		{
			return true;
		}
		else if( Boolean.FALSE.toString().equalsIgnoreCase(bool) )
		{
			return false;
		}
		else
		{
			return defaultValue;
		}
	}

	public void setAttribute(String key, boolean value)
	{
		setAttribute(key, Boolean.toString(value));
	}

	public void setAttribute(String key, String value)
	{
		if( attributes == null )
		{
			attributes = new ArrayList<Attribute>();
		}
		else
		{
			removeAttribute(key);
		}
		attributes.add(new Attribute(key, value));
	}

	public void removeAttribute(String key)
	{
		if( attributes != null )
		{
			for( final Iterator<Attribute> iter = attributes.iterator(); iter.hasNext(); )
			{
				final Attribute att = iter.next();
				if( att.getKey().equals(key) )
				{
					iter.remove();
				}
			}
		}
	}

	@SuppressWarnings("nls")
	@Override
	public String toString()
	{
		return "ID: " + id + ((name != null) ? ", Name: " + name : "");
	}

	@Embeddable
	@AccessType("field")
	public static class Attribute implements Serializable
	{
		private static final long serialVersionUID = 1L;

		@Column(length = 64, nullable = false)
		private String key;
		@Column(name = "value", length = 1024)
		private String value;

		public Attribute()
		{
			super();
		}

		public Attribute(String key, String value)
		{
			this.value = value;
			this.key = key;
		}

		public String getKey()
		{
			return key;
		}

		public void setKey(String key)
		{
			this.key = key;
		}

		public String getValue()
		{
			return value;
		}

		public void setValue(String value)
		{
			this.value = value;
		}
	}
}
