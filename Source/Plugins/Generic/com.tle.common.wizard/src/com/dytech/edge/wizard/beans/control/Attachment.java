/*
 * Created on Jun 22, 2005
 */
package com.dytech.edge.wizard.beans.control;

public class Attachment extends Html
{
	private static final long serialVersionUID = 1;
	public static final String CLASS = "attachment";

	private boolean embed;
	private boolean keepratio;
	private String ratio;

	@Override
	public String getClassType()
	{
		return CLASS;
	}

	public boolean isEmbed()
	{
		return embed;
	}

	public void setEmbed(boolean embed)
	{
		this.embed = embed;
	}

	public boolean isKeepratio()
	{
		return keepratio;
	}

	public void setKeepratio(boolean keepratio)
	{
		this.keepratio = keepratio;
	}

	public String getRatio()
	{
		return ratio;
	}

	public void setRatio(String ratio)
	{
		this.ratio = ratio;
	}
}
