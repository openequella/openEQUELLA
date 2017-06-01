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

package com.tle.common.oauth.beans;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
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

import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.tle.beans.Institution;
import com.tle.common.Check.FieldEquality;

/**
 * @author aholland
 */
@Entity
@AccessType("field")
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"userId", "client_id"})})
public final class OAuthToken implements Serializable, FieldEquality<OAuthToken>
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(nullable = false, length = 255)
	private String userId;

	/**
	 * Purely for efficiency
	 */
	@Column(nullable = false, length = 1024)
	private String username;

	@Index(name = "oauthTokenIndex")
	@Column(nullable = false, length = 255)
	private String token;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@Index(name = "oauthtokenClient")
	private OAuthClient client;

	@Column(nullable = false)
	private Date created;

	/**
	 * Currently, tokens never expire
	 */
	@Column
	private Date expiry;

	/**
	 * The 'code' parameter that initially created this token (if any). You are
	 * supposed to revoke all tokens that were created from a code when the code
	 * is unsuccessfully tried or repeated.
	 */
	@Column(length = 40)
	@Index(name = "oauthtokenCode")
	private String code;

	/**
	 * The set of permissions that the client has requested and the user has
	 * allowed. Currently, apps can do anything TODO: this should be on the an
	 * OAuthApproval object, so that the user only has to approve the client
	 * once and the permissions can be taken from the approval
	 */
	@ElementCollection(fetch = FetchType.EAGER)
	private Set<String> permissions;

	@XStreamOmitField
	@JoinColumn(nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	@Index(name = "tokenInstIndex")
	private Institution institution;

	/**
	 * Hibernate
	 */
	@SuppressWarnings("unused")
	private OAuthToken()
	{
	}

	public OAuthToken(String userId, String username, String token, Date created, OAuthClient client,
		Institution institution)
	{
		this.userId = userId;
		this.username = username;
		this.token = token;
		this.created = created;
		this.client = client;
		this.institution = institution;
	}

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public String getUserId()
	{
		return userId;
	}

	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getToken()
	{
		return token;
	}

	public void setToken(String token)
	{
		this.token = token;
	}

	public OAuthClient getClient()
	{
		return client;
	}

	public void setClient(OAuthClient client)
	{
		this.client = client;
	}

	public Date getCreated()
	{
		return created;
	}

	public void setCreated(Date created)
	{
		this.created = created;
	}

	public Date getExpiry()
	{
		return expiry;
	}

	public void setExpiry(Date expiry)
	{
		this.expiry = expiry;
	}

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public Set<String> getPermissions()
	{
		return permissions;
	}

	public void setPermissions(Set<String> permissions)
	{
		this.permissions = permissions;
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
	public boolean checkFields(OAuthToken rhs)
	{
		return id == rhs.getId();
	}
}
