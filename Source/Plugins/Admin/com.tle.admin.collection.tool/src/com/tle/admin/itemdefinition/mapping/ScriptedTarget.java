/*
 * Created on Oct 11, 2004
 */
package com.tle.admin.itemdefinition.mapping;

import java.util.ArrayList;
import java.util.List;

import com.tle.admin.schema.SchemaNode;

class ScriptedTarget
{
	private SchemaNode target;
	private String cachedPath;
	private List<ScriptedRule> rules;

	public ScriptedTarget()
	{
		super();
		rules = new ArrayList<ScriptedRule>();
	}

	public void destroy()
	{
		if( target != null )
		{
			target.unlock();
		}
	}

	/**
	 * @return Returns the rules.
	 */
	public List<ScriptedRule> getRules()
	{
		return rules;
	}

	/**
	 * @param rules The rules to set.
	 */
	public void setRules(List<ScriptedRule> rules)
	{
		this.rules = rules;
	}

	/**
	 * @return Returns the target.
	 */
	public SchemaNode getTarget()
	{
		return target;
	}

	/**
	 * @param target The target to set.
	 */
	public void setTarget(SchemaNode newTarget)
	{
		if( target != null )
		{
			target.unlock();
		}
		target = newTarget;
		target.lock();
		cachedPath = target.toString();
	}

	@Override
	public String toString()
	{
		return cachedPath;
	}

	@Override
	public int hashCode()
	{
		if( target != null )
		{
			return cachedPath.hashCode();
		}
		else
		{
			return super.hashCode();
		}
	}

	@Override
	public boolean equals(Object obj)
	{
		if( obj instanceof ScriptedTarget )
		{
			ScriptedTarget rhs = (ScriptedTarget) obj;
			return rhs.cachedPath.equals(cachedPath);
		}
		else
		{
			return false;
		}
	}
}
