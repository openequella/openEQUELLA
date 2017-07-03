package com.tle.common.usermanagement.user.valuebean;

public class EmailOnlyUserBean implements UserBean
{
	private static final long serialVersionUID = 1L;

	private final String id;
	private final String email;

	@SuppressWarnings("nls")
	public EmailOnlyUserBean(String email)
	{
		this.id = "email:" + email;
		this.email = email;
	}

	@Override
	public String getUniqueID()
	{
		return id;
	}

	@Override
	public String getUsername()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getFirstName()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getLastName()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getEmailAddress()
	{
		return email;
	}

	@Override
	public boolean equals(Object obj)
	{
		if( this == obj )
		{
			return true;
		}

		if( !(obj instanceof UserBean) )
		{
			return false;
		}

		return id.equals(((UserBean) obj).getUniqueID());
	}

	@Override
	public int hashCode()
	{
		return id.hashCode();
	}
}
