package com.tle.common.lti.consumers.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.tle.beans.entity.BaseEntity;

@Entity
@AccessType("field")
public class LtiConsumer extends BaseEntity
{
	@Index(name = "consumerKey")
	@Column(length = 255, nullable = false)
	private String consumerKey;
	@Column(length = 255, nullable = false)
	private String consumerSecret;

	@Column(length = 50)
	private String prefix;
	@Column(length = 50)
	private String postfix;
	@Column(length = 255)
	private String allowedExpression;
	@ElementCollection(fetch = FetchType.LAZY)
	private Set<String> instructorRoles;
	@ElementCollection(fetch = FetchType.LAZY)
	private Set<String> otherRoles;
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "lti_consumer_id", nullable = false)
	private Set<LtiConsumerCustomRole> customRoles = new HashSet<LtiConsumerCustomRole>();
	@Column
	private int unknownUser;
	@ElementCollection(fetch = FetchType.LAZY)
	private Set<String> unknownGroups;

	public String getConsumerKey()
	{
		return consumerKey;
	}

	public void setConsumerKey(String consumerKey)
	{
		this.consumerKey = consumerKey;
	}

	public String getConsumerSecret()
	{
		return consumerSecret;
	}

	public void setConsumerSecret(String consumerSecret)
	{
		this.consumerSecret = consumerSecret;
	}

	public int getUnknownUser()
	{
		return unknownUser;
	}

	public void setUnknownUser(int unknownUser)
	{
		this.unknownUser = unknownUser;
	}

	public String getPrefix()
	{
		return prefix;
	}

	public void setPrefix(String prefix)
	{
		this.prefix = prefix;
	}

	public String getPostfix()
	{
		return postfix;
	}

	public void setPostfix(String postfix)
	{
		this.postfix = postfix;
	}

	public Set<String> getOtherRoles()
	{
		return otherRoles;
	}

	public void setOtherRoles(Set<String> otherRoles)
	{
		this.otherRoles = otherRoles;
	}

	public String getAllowedExpression()
	{
		return allowedExpression;
	}

	public void setAllowedExpression(String allowedExpression)
	{
		this.allowedExpression = allowedExpression;
	}

	public Set<String> getInstructorRoles()
	{
		return instructorRoles;
	}

	public void setInstructorRoles(Set<String> instructorRoles)
	{
		this.instructorRoles = instructorRoles;
	}

	public Set<String> getUnknownGroups()
	{
		return unknownGroups;
	}

	public void setUnknownGroups(Set<String> unknownGroups)
	{
		this.unknownGroups = unknownGroups;
	}

	public Set<LtiConsumerCustomRole> getCustomRoles()
	{
		return customRoles;
	}

	public void setCustomRoles(Set<LtiConsumerCustomRole> customRoles)
	{
		this.customRoles = customRoles;
	}

}
