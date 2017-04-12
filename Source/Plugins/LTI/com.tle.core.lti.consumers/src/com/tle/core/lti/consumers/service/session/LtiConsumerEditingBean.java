package com.tle.core.lti.consumers.service.session;

import java.util.Set;

import com.tle.common.Pair;
import com.tle.core.services.entity.EntityEditingBean;

public class LtiConsumerEditingBean extends EntityEditingBean
{
	private String consumerKey;
	private String consumerSecret;
	private String prefix;
	private String postfix;
	private String allowedExpression;
	private Set<String> instructorRoles;
	private Set<String> otherRoles;
	private int unknownUser;
	private Set<String> unknownGroups;
	private Set<Pair<String, String>> customRoles;

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

	public Set<String> getInstructorRoles()
	{
		return instructorRoles;
	}

	public void setInstructorRoles(Set<String> instructorRoles)
	{
		this.instructorRoles = instructorRoles;
	}

	public Set<String> getOtherRoles()
	{
		return otherRoles;
	}

	public void setOtherRoles(Set<String> otherRoles)
	{
		this.otherRoles = otherRoles;
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

	public String getAllowedExpression()
	{
		return allowedExpression;
	}

	public void setAllowedExpression(String allowedExpression)
	{
		this.allowedExpression = allowedExpression;
	}

	public Set<String> getUnknownGroups()
	{
		return unknownGroups;
	}

	public void setUnknownGroups(Set<String> unknownGroups)
	{
		this.unknownGroups = unknownGroups;
	}

	public Set<Pair<String, String>> getCustomRoles()
	{
		return customRoles;
	}

	public void setCustomRoles(Set<Pair<String, String>> customRoles)
	{
		this.customRoles = customRoles;
	}

}
