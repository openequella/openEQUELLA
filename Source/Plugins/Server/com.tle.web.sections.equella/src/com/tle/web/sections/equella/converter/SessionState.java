package com.tle.web.sections.equella.converter;

import java.io.Serializable;

public interface SessionState extends Serializable
{
	boolean isRemoved();

	boolean isModified();

	String getSessionId();

	String getBookmarkString();

	void setBookmarkString(String id);

	boolean isNew();

	void synced();
}
