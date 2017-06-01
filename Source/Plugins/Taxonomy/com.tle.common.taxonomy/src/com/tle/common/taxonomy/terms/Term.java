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

package com.tle.common.taxonomy.terms;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;

import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.tle.common.Check;
import com.tle.common.DontUseMethod;
import com.tle.common.taxonomy.Taxonomy;

/**
 * The hierarchy is stored using the Modified Pre-order Tree Traversal technique
 * described at http://www.sitepoint.com/article/hierarchical-data-database/2/
 * <p>
 * The uniqueness constraint is on taxonomy/parent/value rather than just
 * taxonomy/fullvalue, as the full value can be set to NULL indicating that it
 * is invalid. This allows for costly full value processing to be deferred until
 * after various operations have occurred.
 * 
 * @author Nicholas Read
 */
@Entity
@AccessType("field")
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"taxonomy_id", "parent_id", "valueHash"})})
@NamedQueries({
		@NamedQuery(name = "incLeft", query = "UPDATE Term SET lft = lft + :amount WHERE lft >= :from AND lft <= :to AND taxonomy = :taxonomy", cacheable = true),
		@NamedQuery(name = "incRight", query = "UPDATE Term SET rht = rht + :amount WHERE rht >= :from AND rht <= :to AND taxonomy = :taxonomy", cacheable = true),
		@NamedQuery(name = "decLeft", query = "UPDATE Term SET lft = lft - :amount WHERE lft >= :from AND lft <= :to AND taxonomy = :taxonomy", cacheable = true),
		@NamedQuery(name = "decRight", query = "UPDATE Term SET rht = rht - :amount WHERE rht >= :from AND rht <= :to AND taxonomy = :taxonomy", cacheable = true)})
public class Term  implements Serializable
{
	private static final long serialVersionUID = 1L;
	public static final int MAX_TERM_VALUE_LENGTH = 1024;
	// SQL Server limits us to 4000, otherwise we need to make it a @Lob, then
	// Oracle can't search over it.
	public static final int MAX_TERM_FULLVALUE_LENGTH = 4000;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@JoinColumn(nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	@Index(name = "term_taxonomy")
	@XStreamOmitField
	private Taxonomy taxonomy;

	@Column(length = 40, nullable = false)
	@Index(name = "termUuidIndex")
	private String uuid;

	// Cannot be Indexed due to SQL server being a fail whale
	@Column(length = MAX_TERM_VALUE_LENGTH, nullable = false)
	private String value;

	// Computed MD5 hash of value
	@Column(length = 32)
	private String valueHash;

	// Cannot be NotNull, it is temporarily set to null during term insertions.
	// Cannot be Indexed due to SQL server being a fail whale
	@Column(length = MAX_TERM_FULLVALUE_LENGTH)
	private String fullValue;

	@ManyToOne(fetch = FetchType.LAZY)
	@Index(name = "term_parent")
	private Term parent;

	@Column(name = "lft")
	@Index(name = "term_left_position")
	@XStreamOmitField
	private int left;

	@Column(name = "rht")
	@Index(name = "term_right_position")
	@XStreamOmitField
	private int right;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@Fetch(value = FetchMode.SUBSELECT)
	@JoinColumn(name = "term_id", nullable = false)
	@MapKey(name = "key")
	@DontUseMethod
	private Map<String, TermAttribute> attributes;

	public Term()
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

	public Taxonomy getTaxonomy()
	{
		return taxonomy;
	}

	public void setTaxonomy(Taxonomy taxonomy)
	{
		this.taxonomy = taxonomy;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
		this.valueHash = DigestUtils.md5Hex(value);
	}

	public String getValueHash()
	{
		return valueHash;
	}

	public String getFullValue()
	{
		return fullValue;
	}

	public void setFullValue(String fullValue)
	{
		this.fullValue = fullValue;
	}

	public Term getParent()
	{
		return parent;
	}

	public void setParent(Term parent)
	{
		this.parent = parent;
	}

	public int getLeft()
	{
		return left;
	}

	public void setLeft(int left)
	{
		this.left = left;
	}

	public int getRight()
	{
		return right;
	}

	public void setRight(int right)
	{
		this.right = right;
	}

	public boolean isLeaf()
	{
		return right - left == 1;
	}

	@Override
	public boolean equals(Object obj)
	{
		if( obj == null )
		{
			// Non-null
			return false;
		}
		else if( this == obj )
		{
			// Reflexitivity
			return true;
		}
		else if( getClass() != obj.getClass() )
		{
			// Symmetry
			return false;
		}
		else
		{
			return id == ((Term) obj).getId();
		}
	}

	public boolean checkFields(Term rhs)
	{
		return Objects.equals(id, rhs.id);
	}

	@Override
	public int hashCode()
	{
		return Check.getHashCode(id);
	}

	public Map<String, String> getAttributes()
	{
		ensureAttributes();
		Map<String, String> results = new HashMap<String, String>();
		for( TermAttribute attribute : attributes.values() )
		{
			results.put(attribute.getKey(), attribute.getValue());
		}
		return results;
	}

	private void ensureAttributes()
	{
		if( attributes == null )
		{
			attributes = new HashMap<String, TermAttribute>();
		}
	}

	public void setAttributes(Map<String, String> values)
	{
		ensureAttributes();
		attributes.clear();
		if( values != null )
		{
			for( Map.Entry<String, String> entry : values.entrySet() )
			{
				attributes.put(entry.getKey(), new TermAttribute(entry.getKey(), entry.getValue()));
			}
		}
	}

	public String getAttribute(String key)
	{
		if( attributes != null )
		{
			TermAttribute tattr = attributes.get(key);
			if( tattr != null )
			{
				return tattr.getValue();
			}
		}
		return null;
	}

	public void setAttribute(String key, String value)
	{
		ensureAttributes();
		attributes.put(key, new TermAttribute(key, value));
	}

	public void removeAttribute(String key)
	{
		if( attributes != null )
		{
			attributes.remove(key);
		}
	}

	@Entity(name = "TermAttributes")
	@AccessType("field")
	public static class TermAttribute
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		@XStreamOmitField
		long id;

		@ManyToOne
		@JoinColumn(name = "term_id", insertable = false, nullable = false, updatable = false)
		@XStreamOmitField
		@Index(name = "termAttrIndex")
		private Term term;

		@Column(length = 64, nullable = false)
		private String key;

		@Lob
		private String value;

		public TermAttribute()
		{
			// Required by Hibernate
		}

		public TermAttribute(String key, String value)
		{
			this.key = key;
			this.value = value;
		}

		public String getKey()
		{
			return key;
		}

		public String getValue()
		{
			return Check.nullToEmpty(value);
		}

		public Term getTerm()
		{
			return term;
		}
	}
}
