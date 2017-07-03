/*
 * Created on May 25, 2005
 */
package com.tle.common.settings.standard;

import com.tle.common.settings.ConfigurationProperties;
import com.tle.common.settings.annotation.Property;

/**
 * @author Nicholas Read
 */
public class MailSettings implements ConfigurationProperties
{
	private static final long serialVersionUID = 1;

	@Property(key = "mail.from")
	private String sender;

	@Property(key = "mail.from.name")
	private String senderName;

	@Property(key = "mail.host")
	private String server;

	@Property(key = "mail.transport.protocol")
	private String protocol;

	@Property(key = "mail.username")
	private String username;

	@Property(key = "mail.password")
	private String password;

	public String getProtocol()
	{
		return protocol;
	}

	public void setProtocol(String protocol)
	{
		this.protocol = protocol;
	}

	public String getSender()
	{
		return sender;
	}

	public void setSender(String sender)
	{
		this.sender = sender;
	}

	/**
	 * @return The display name of the sender
	 */
	public String getSenderName()
	{
		return senderName;
	}

	/**
	 * @param senderName The display name of the sender
	 */
	public void setSenderName(String senderName)
	{
		this.senderName = senderName;
	}

	/**
	 * @return Returns the server.
	 */
	public String getServer()
	{
		return server;
	}

	public void setServer(String server)
	{
		this.server = server;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}
}
