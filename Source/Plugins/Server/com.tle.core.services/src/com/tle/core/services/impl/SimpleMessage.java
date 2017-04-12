package com.tle.core.services.impl;

import java.io.Serializable;

public class SimpleMessage implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final String messageId;
	private final Serializable contents;

	public SimpleMessage(String messageId, Serializable contents)
	{
		this.messageId = messageId;
		this.contents = contents;
	}

	@SuppressWarnings("unchecked")
	public <T> T getContents()
	{
		return (T) contents;
	}

	public String getMessageId()
	{
		return messageId;
	}

}
