/*
 * Created on Jun 23, 2005
 */
package com.tle.beans.entity.itemdef.mapping;

import java.io.Serializable;

@SuppressWarnings("nls")
public class HTMLMapping implements Serializable
{
	private static final long serialVersionUID = 1;

	private String html;
	private String itemdef;

	public HTMLMapping()
	{
		html = "";
		itemdef = "";
	}

	public String getHtml()
	{
		return html;
	}

	public void setHtml(String ims)
	{
		this.html = ims;
	}

	public String getItemdef()
	{
		return itemdef;
	}

	public void setItemdef(String itemdef)
	{
		this.itemdef = itemdef;
	}
}
