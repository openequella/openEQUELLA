/*
 * Created on 13/04/2006
 */
package com.dytech.edge.admin.script.basicmodel;

import com.dytech.edge.admin.script.ifmodel.Equality;
import com.dytech.edge.admin.script.ifmodel.Equals;
import com.dytech.edge.admin.script.ifmodel.NotEquals;

public abstract class UserMethodMethod
{
	public abstract String toScript();

	public abstract Equality getOp();

	public String toEasyRead()
	{
		return getOp().toEasyRead();
	}

	public static class HasRole extends UserMethodMethod
	{
		@Override
		public String toScript()
		{
			return "hasRole";
		}

		@Override
		public Equality getOp()
		{
			return new Equals();
		}
	}

	public static class DoesntHasRole extends UserMethodMethod
	{
		@Override
		public String toScript()
		{
			return "doesntHaveRole";
		}

		@Override
		public Equality getOp()
		{
			return new NotEquals();
		}
	}
}
