/*
 * Created on Feb 17, 2005
 */
package com.dytech.edge.common.valuebean;

import java.util.Objects;

import com.tle.common.Check;
import com.tle.common.Check.FieldEquality;
import com.tle.common.Format;

/**
 * @author adame
 */
public class DefaultUserBean implements UserBean, FieldEquality<DefaultUserBean>
{
	private static final long serialVersionUID = 1L;
	private final String id;
	private final String username;
	private final String firstName;
	private final String lastName;
	private final String emailAddress;

	public DefaultUserBean(String id, String username, String firstName, String lastName, String emailAddress)
	{
		Check.checkNotNull(id);
		Check.checkNotNull(username);
		Check.checkNotNull(firstName);
		Check.checkNotNull(lastName);

		this.id = id;
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
		this.emailAddress = emailAddress;
	}

	@Override
	public String getUniqueID()
	{
		return id;
	}

	@Override
	public String getUsername()
	{
		return username;
	}

	@Override
	public String getFirstName()
	{
		return firstName;
	}

	@Override
	public String getLastName()
	{
		return lastName;
	}

	@Override
	public String getEmailAddress()
	{
		return emailAddress;
	}

	@Override
	public int hashCode()
	{
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		return Check.commonEquals(this, obj);
	}

	@Override
	public boolean checkFields(DefaultUserBean rhs)
	{
		return Objects.equals(id, rhs.id);
	}

	@Override
	public String toString()
	{
		return Format.format(this);
	}
}
