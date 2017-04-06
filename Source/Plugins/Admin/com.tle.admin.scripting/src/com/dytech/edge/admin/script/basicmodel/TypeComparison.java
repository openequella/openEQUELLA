package com.dytech.edge.admin.script.basicmodel;

import com.dytech.edge.admin.script.ifmodel.Comparison;
import com.dytech.edge.admin.script.ifmodel.Equality;
import com.dytech.edge.admin.script.ifmodel.Equals;
import com.dytech.edge.admin.script.ifmodel.IfModel;
import com.tle.admin.Driver;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.util.UserBeanUtils;
import com.tle.core.remoting.RemoteUserService;

public class TypeComparison implements Comparison
{
	protected UserMethodMethod op;
	protected String value;
	private final RemoteUserService userService;

	public TypeComparison()
	{
		userService = Driver.instance().getClientService().getService(RemoteUserService.class);
	}

	public TypeComparison(UserMethodMethod op, String value)
	{
		this();
		this.op = op;
		this.value = value;
	}

	public TypeComparison(Equality equality, String value2)
	{
		this();
		if( equality instanceof Equals )
		{
			op = new UserMethodMethod.HasRole();
		}
		else
		{
			op = new UserMethodMethod.DoesntHasRole();
		}
		this.value = value2;
	}

	public UserMethodMethod getOperation()
	{
		return op;
	}

	public void setOp(UserMethodMethod op)
	{
		this.op = op;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	@Override
	public String toScript()
	{
		return "user." + op.toScript() + "('" + IfModel.encode(value) + "')";
	}

	@Override
	public String toEasyRead()
	{
		return CurrentLocale.get("com.dytech.edge.admin.script.target.role") + " " + op.toEasyRead() + " '"
			+ UserBeanUtils.getRole(userService, value) + "'";
	}
}
