package com.tle.common.scripting.types.impl;

import com.dytech.edge.common.valuebean.DefaultRoleBean;
import com.dytech.edge.common.valuebean.RoleBean;
import com.tle.common.scripting.types.RoleScriptType;

/**
 * @author aholland
 */
public class RoleScriptTypeImpl extends DefaultRoleBean implements RoleScriptType
{
	private static final long serialVersionUID = 1L;

	public RoleScriptTypeImpl(RoleBean role)
	{
		super(role.getUniqueID(), role.getName());
	}
}
