/*
 * Created on Oct 11, 2004
 */
package com.tle.admin.itemdefinition.mapping;

class ScriptedRule
{
	private String literal;
	private String script;

	public ScriptedRule()
	{
		super();
	}

	/**
	 * @return Returns the literal.
	 */
	public String getLiteral()
	{
		return literal;
	}

	/**
	 * @param literal The literal to set.
	 */
	public void setLiteral(String literal)
	{
		this.literal = literal;
	}

	/**
	 * @return Returns the script.
	 */
	public String getScript()
	{
		return script;
	}

	/**
	 * @param script The script to set.
	 */
	public void setScript(String script)
	{
		this.script = script;
	}
}
