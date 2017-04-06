/*
 * Created on Jun 22, 2005
 */
package com.dytech.edge.wizard.beans.control;

public class ShuffleList extends Multi
{
	private static final long serialVersionUID = 1;
	public static final String CLASS1 = "shufflelist";

	private boolean tokenise = true;
	private boolean forceUnique;
	private boolean checkDuplication;

	@Override
	public String getClassType()
	{
		return CLASS1;
	}

	public boolean isTokenise()
	{
		return tokenise;
	}

	public void setTokenise(boolean tokenise)
	{
		this.tokenise = tokenise;
	}

	public boolean isCheckDuplication()
	{
		return checkDuplication;
	}

	public void setCheckDuplication(boolean checkDuplication)
	{
		this.checkDuplication = checkDuplication;
	}

	public boolean isForceUnique()
	{
		return forceUnique;
	}

	public void setForceUnique(boolean forceUnique)
	{
		this.forceUnique = forceUnique;
	}
}
