package com.tle.web.api.item.interfaces.beans;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tle.web.api.interfaces.beans.UserBean;

public class ItemNodeStatusMessageBean
{
	public enum MessageType
	{
		accept, reject, submit, comment
	}

	private final MessageType type;
	private final UserBean user;
	private final String message;
	private final Date date;

	@JsonCreator
	public ItemNodeStatusMessageBean(@JsonProperty("type") MessageType type, @JsonProperty("user") UserBean user,
		@JsonProperty("message") String message, @JsonProperty("date") Date date)
	{
		this.type = type;
		this.user = user;
		this.message = message;
		this.date = date;
	}

	public MessageType getType()
	{
		return type;
	}

	public UserBean getUser()
	{
		return user;
	}

	public String getMessage()
	{
		return message;
	}

	public Date getDate()
	{
		return date;
	}
}
