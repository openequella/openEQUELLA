package com.tle.common.wizard.controls.userselector;

import java.util.Set;

import com.dytech.edge.wizard.beans.control.CustomControl;

@SuppressWarnings("nls")
public class UserSelectorControl extends CustomControl
{
	private static final long serialVersionUID = 1L;
	private static final String KEY_SELECT_MULTIPLE = "IsSelectMultiple";
	private static final String KEY_RESTRICT_SWITCH_PREFIX = "IsRestricted";
	private static final String KEY_RESTRICT_CHOICES_PREFIX = "RestrictTo";

	public static final String KEY_RESTRICT_USER_GROUPS = "Groups";

	public UserSelectorControl()
	{
		setClassType("userselector");
	}

	public UserSelectorControl(CustomControl cloned)
	{
		if( cloned != null )
		{
			cloned.cloneTo(this);
		}
	}

	public boolean isSelectMultiple()
	{
		return getBooleanAttribute(KEY_SELECT_MULTIPLE);
	}

	public void setSelectMultiple(boolean multiple)
	{
		getAttributes().put(KEY_SELECT_MULTIPLE, multiple);
	}

	public boolean isRestricted(String key)
	{
		return getBooleanAttribute(KEY_RESTRICT_SWITCH_PREFIX + key);
	}

	public void setRestricted(String key, boolean b)
	{
		getAttributes().put(KEY_RESTRICT_SWITCH_PREFIX + key, b);
	}

	/**
	 * @param key
	 * @return The set of restrictions for the given key. Will never return
	 *         null.
	 */
	public Set<String> getRestrictedTo(String key)
	{
		return ensureSetAttribute(KEY_RESTRICT_CHOICES_PREFIX + key);
	}
}
