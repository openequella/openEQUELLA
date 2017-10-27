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

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.tle.beans.Institution;
import com.tle.common.Format;
import com.tle.common.usermanagement.user.valuebean.UserBean;

@Entity
@AccessType("field")
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"uuid", "institution_id"})})
public class TLEUser implements UserBean
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Index(name = "userUuidIndex")
	@Column(length = 40, nullable = false)
	private String uuid;

	@JoinColumn(nullable = false)
	@Index(name = "userInstitutionIndex")
	@ManyToOne(fetch = FetchType.LAZY)
	private Institution institution;

	@Column(nullable = false)
	private String username;
	@Column(nullable = false)
	private String firstName;
	@Column(nullable = false)
	private String lastName;
	private String emailAddress;
	@Column(nullable = false)
	private String password;

	public TLEUser()
	{
		super();
	}

	@Override
	public String getEmailAddress()
	{
		return emailAddress;
	}

	public void setEmailAddress(String email)
	{
		this.emailAddress = email;
	}

	@Override
	public String getFirstName()
	{
		return firstName;
	}

	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public Institution getInstitution()
	{
		return institution;
	}

	public void setInstitution(Institution institution)
	{
		this.institution = institution;
	}

	@Override
	public String getLastName()
	{
		return lastName;
	}

	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}

	@Override
	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	@Override
	public String getUniqueID()
	{
		return getUuid();
	}

	@Override
	public int hashCode()
	{
		return getUuid().hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if( obj instanceof TLEUser )
		{
			TLEUser rhs = (TLEUser) obj;
			return Objects.equals(id, rhs.id);
		}
		return false;
	}

	@Override
	public String toString()
	{
		return Format.format(this);
	}
}
