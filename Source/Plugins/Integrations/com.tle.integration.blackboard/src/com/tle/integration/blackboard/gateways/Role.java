/*
 * Created on Jun 18, 2004
 */
package com.tle.integration.blackboard.gateways;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.dytech.devlib.PropBagEx;
import com.tle.common.NameValue;

/**
 * Repesents an LDAP role.
 */
public class Role extends NameValue
{
	private static final long serialVersionUID = 1L;
	public static final Map<String, String> ROLE_NAMES = new HashMap<String, String>();
	@SuppressWarnings("nls")
	protected static final String[][] ROLES = new String[][]{
			{"search", "student", "teacher", "metadatareviewer", "admin",},
			{"Basic search rights", "Student Access", "Teacher Access", "Metadata Reviewer", "Administrator",},};

	static
	{
		for( int i = 0; i < ROLES[0].length; i++ )
		{
			ROLE_NAMES.put(ROLES[0][i], ROLES[1][i]);
			ROLE_NAMES.put(ROLES[1][i], ROLES[0][i]);
		}
	}

	public Role(String uuid, String name)
	{
		super(name, uuid);
	}

	@SuppressWarnings("nls")
	public Role(PropBagEx xml)
	{
		super(xml.getNode("@value"), xml.getNode("@name"));
	}

	@SuppressWarnings("nls")
	public PropBagEx getXml()
	{
		PropBagEx xml = new PropBagEx();
		xml = xml.newSubtree("role");
		xml.setNode("@name", getValue());
		xml.setNode("@value", getName());
		return xml;
	}

	public static List<Role> getRoles(PropBagEx xml)
	{
		Map<String, Role> map = getRoleMap(xml);
		List<Role> list = new ArrayList<Role>(map.size());
		list.addAll(map.values());
		return list;
	}

	public static Map<String, Role> getRoleMap(PropBagEx xml)
	{
		Map<String, Role> map = new LinkedHashMap<String, Role>();

		for( PropBagEx rolexml : xml.iterator("role") ) //$NON-NLS-1$
		{
			Role role = new Role(rolexml);
			map.put(role.getValue(), role);
		}

		return map;
	}
}
