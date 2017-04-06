/**
 * 
 */
package com.tle.upgrade;

public class UpgradeDepends
{
	private final String id;
	private boolean obsoletes;
	private boolean fixes;

	public UpgradeDepends(String id)
	{
		this.id = id;
	}

	public boolean isObsoletes()
	{
		return obsoletes;
	}

	public void setObsoletes(boolean obsoletes)
	{
		this.obsoletes = obsoletes;
	}

	public boolean isFixes()
	{
		return fixes;
	}

	public void setFixes(boolean fixes)
	{
		this.fixes = fixes;
	}

	public String getId()
	{
		return id;
	}
}