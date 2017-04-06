/*
 * Created on Mar 8, 2005
 */
package com.tle.admin.usermanagement.ldap;

import java.util.HashMap;
import java.util.Map;

import com.tle.common.i18n.CurrentLocale;

@SuppressWarnings("nls")
public final class LDAPPresets
{
	private static final String[] ATTRIBUTES_NAMES = new String[]{"username", "id", "groupId", "groupName", "surname",
			"givenname", "email", "memberOf", "member", "memberKey"};

	private static final String[] AD_VALUES = new String[]{"userPrincipalName", "sAMAccountName", "sAMAccountName",
			"name", "sn", "givenName", "mail", "memberOf", "member", ""};

	private static final String[] OSX_VALUES = new String[]{"uid", "uid", "cn", "cn", "cn", "", "", "", "memberUid",
			"uid"};

	private static final String[] SUNONE_VALUES = new String[]{"uid", "uid", "uid", "cn", "sn", "givenname", "mail",
			"", "", ""};

	private static final String[] BLANK_VALUES = new String[]{"", "", "", "", "", "", "", "", "", ""};

	public static Preset generateActiviteDirectory()
	{
		return generate(CurrentLocale.get("com.tle.admin.usermanagement.ldap.ldappresets.active"), "person", "group",
			AD_VALUES);
	}

	public static Preset generateOSX()
	{
		return generate(CurrentLocale.get("com.tle.admin.usermanagement.ldap.ldappresets.mac"), "inetOrgPerson",
			"posixGroup", OSX_VALUES);
	}

	public static Preset generateSunOne()
	{
		return generate(CurrentLocale.get("com.tle.admin.usermanagement.ldap.ldappresets.sun"), "person", "group",
			SUNONE_VALUES);
	}

	private static Preset generateBlank()
	{
		return generate(CurrentLocale.get("com.tle.admin.usermanagement.ldap.ldappresets.select"), "", "", BLANK_VALUES);
	}

	private static Preset generate(String name, String user, String group, String[] values)
	{
		Preset preset = new Preset();
		preset.setName(name);
		preset.setUserObject(user);
		preset.setGroupObject(group);
		Map<String, String> map = new HashMap<String, String>();
		for( int i = 0; i < values.length; i++ )
		{
			map.put(ATTRIBUTES_NAMES[i], values[i]);
		}
		preset.setValues(map);
		return preset;
	}

	public static Preset[] getAll()
	{
		return new Preset[]{generateBlank(), generateActiviteDirectory(), generateOSX(), generateSunOne()};
	}

	public static class Preset
	{
		private String name;
		private Map<String, String> values;
		private String userObject;
		private String groupObject;

		public String getGroupObject()
		{
			return groupObject;
		}

		public void setGroupObject(String groupObject)
		{
			this.groupObject = groupObject;
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public String getUserObject()
		{
			return userObject;
		}

		public void setUserObject(String userObject)
		{
			this.userObject = userObject;
		}

		public Map<String, String> getValues()
		{
			return values;
		}

		public void setValues(Map<String, String> values)
		{
			this.values = values;

		}

		@Override
		public String toString()
		{
			return name;
		}
	}

	private LDAPPresets()
	{
		throw new Error();
	}
}
