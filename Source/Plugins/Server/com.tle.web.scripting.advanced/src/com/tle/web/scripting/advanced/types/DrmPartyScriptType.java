package com.tle.web.scripting.advanced.types;

import java.io.Serializable;

/**
 * @author aholland
 */
public interface DrmPartyScriptType extends Serializable
{
	String getEmail();

	void setEmail(String email);

	String getName();

	void setName(String name);

	String getUserID();

	void setUserID(String uid);

	boolean isOwner();

	void setOwner(boolean owner);
}
